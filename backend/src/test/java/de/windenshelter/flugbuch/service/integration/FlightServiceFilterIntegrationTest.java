package de.windenshelter.flugbuch.service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import de.windenshelter.flugbuch.dto.FlightLogEntryDto;
import de.windenshelter.flugbuch.dto.FlightSearchCriteria;
import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.repository.MainFlightLogStagingRepository;
import de.windenshelter.flugbuch.service.FlightService;

/**
 * Exercises {@link FlightService#findAll(FlightSearchCriteria, Pageable)}
 * against a real (H2) database, since the MockMvc controller tests mock out
 * {@link FlightService} entirely and never actually run the
 * {@code FlightSpecifications} predicates or the sort-field translation.
 * This is what proves the case-insensitive partial ("contains") matching,
 * date filtering, pagination and sorting really work against a persistence
 * layer, not just that the controller wires parameters through correctly.
 */
@SpringBootTest
@Transactional
class FlightServiceFilterIntegrationTest {

    @Autowired
    private FlightService flightService;

    @Autowired
    private MainFlightLogStagingRepository stagingRepository;

    @BeforeEach
    void setUp() {
        stagingRepository.deleteAll();

        stagingRepository.save(flight(1, "Max Mustermann", "ASK 21", "D-1234", "Schulung",
                LocalDate.of(2026, 7, 26)));
        stagingRepository.save(flight(2, "Erika Musterfrau", "Duo Discus", "D-5678", "Streckenflug",
                LocalDate.of(2026, 7, 20)));
        stagingRepository.save(flight(3, "Max Mustermann", "Duo Discus", "D-5678", "Streckenflug",
                LocalDate.of(2026, 6, 15)));
        stagingRepository.save(flight(4, "Klaus Kleiner", "ASK 21", "D-9999", "Schulung",
                LocalDate.of(2026, 7, 10)));
    }

    private StagingMainFlightLog flight(int externalId, String pilot, String aircraftType,
            String registration, String flightType, LocalDate date) {
        return StagingMainFlightLog.builder()
                .externalId(externalId)
                .pilot(pilot)
                .muster(aircraftType)
                .kennzeichen(registration)
                .flugart(flightType)
                .datum(date)
                .build();
    }

    // No filter at all: every entry in the table comes back.
    @Test
    void findAll_noCriteria_returnsEverything() {
        Page<FlightLogEntryDto> result = flightService.findAll(new FlightSearchCriteria(), Pageable.unpaged());

        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    // "max" (lowercase, partial) should still find "Max Mustermann".
    @Test
    void findAll_partialPilotMatch_isCaseInsensitiveContains() {
        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setPilot("max");

        Page<FlightLogEntryDto> result = flightService.findAll(criteria, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(dto -> dto.getPilot().equals("Max Mustermann"));
    }

    // "discus" (partial) should find both "Duo Discus" entries.
    @Test
    void findAll_partialAircraftTypeMatch_findsAllVariants() {
        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setAircraftType("discus");

        Page<FlightLogEntryDto> result = flightService.findAll(criteria, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(dto -> dto.getAircraftType().equals("Duo Discus"));
    }

    // Filtering by an exact date returns only that day's entry.
    @Test
    void findAll_exactDate_returnsOnlyThatDay() {
        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setDate(LocalDate.of(2026, 7, 26));

        Page<FlightLogEntryDto> result = flightService.findAll(criteria, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPilot()).isEqualTo("Max Mustermann");
    }

    // A dateFrom/dateTo range excludes the one entry that falls outside it.
    @Test
    void findAll_dateRange_excludesOutOfRangeEntries() {
        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setDateFrom(LocalDate.of(2026, 7, 1));
        criteria.setDateTo(LocalDate.of(2026, 7, 31));

        Page<FlightLogEntryDto> result = flightService.findAll(criteria, Pageable.unpaged());

        // Excludes the 15.06.2026 entry, which falls outside the range.
        assertThat(result.getContent()).hasSize(3);
    }

    // Two filters together (AND): only the entry matching both remains.
    @Test
    void findAll_combinedPilotAndAircraftType_narrowsToSingleEntry() {
        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setPilot("Max");
        criteria.setAircraftType("ASK");

        Page<FlightLogEntryDto> result = flightService.findAll(criteria, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRegistration()).isEqualTo("D-1234");
    }

    // A filter matching nobody returns an empty (not null/error) page.
    @Test
    void findAll_noMatches_returnsEmptyPage() {
        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setPilot("Nonexistent Pilot");

        Page<FlightLogEntryDto> result = flightService.findAll(criteria, Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void findAll_pilotFilterContainingWildcardCharacters_isTreatedLiterally() {
        // Regression guard: a raw "%" in user input must not act as a SQL wildcard.
        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setPilot("%");

        Page<FlightLogEntryDto> result = flightService.findAll(criteria, Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    // -------------------------------------------------------------------
    // Pagination
    // -------------------------------------------------------------------

    // Page 1 of 2 (size 2 of 4 total), sorted by date ascending: checks content + page metadata.
    @Test
    void findAll_pageSizeTwo_returnsFirstPageWithCorrectMetadata() {
        Pageable firstPage = PageRequest.of(0, 2, Sort.by("date").ascending());

        Page<FlightLogEntryDto> result = flightService.findAll(new FlightSearchCriteria(), firstPage);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isZero();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
        // Ascending by date: 15.06 then 10.07.
        assertThat(result.getContent().get(0).getDate()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(result.getContent().get(1).getDate()).isEqualTo(LocalDate.of(2026, 7, 10));
    }

    // The second (last) page of the same 4-entry, size-2 pagination.
    @Test
    void findAll_pageSizeTwo_returnsSecondPage() {
        Pageable secondPage = PageRequest.of(1, 2, Sort.by("date").ascending());

        Page<FlightLogEntryDto> result = flightService.findAll(new FlightSearchCriteria(), secondPage);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
        assertThat(result.getContent().get(0).getDate()).isEqualTo(LocalDate.of(2026, 7, 20));
        assertThat(result.getContent().get(1).getDate()).isEqualTo(LocalDate.of(2026, 7, 26));
    }

    // Filtering and pagination combined: page metadata reflects only the filtered subset.
    @Test
    void findAll_filterAndPaginateTogether_returnsFilteredPage() {
        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setAircraftType("ASK");

        Page<FlightLogEntryDto> result = flightService.findAll(criteria, PageRequest.of(0, 1));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    // -------------------------------------------------------------------
    // Sorting (proves the DTO-field-name -> entity-field-name translation)
    // -------------------------------------------------------------------

    // Sorting by "date" descending only works if the entity-field translation (date -> datum) is applied.
    @Test
    void findAll_sortByDateDescending_ordersNewestFirst() {
        Pageable sorted = PageRequest.of(0, 10, Sort.by("date").descending());

        Page<FlightLogEntryDto> result = flightService.findAll(new FlightSearchCriteria(), sorted);

        assertThat(result.getContent()).extracting(FlightLogEntryDto::getDate)
                .containsExactly(
                        LocalDate.of(2026, 7, 26),
                        LocalDate.of(2026, 7, 20),
                        LocalDate.of(2026, 7, 10),
                        LocalDate.of(2026, 6, 15));
    }

    // Sorting by pilot name, ascending: alphabetical order.
    @Test
    void findAll_sortByPilotAscending_ordersAlphabetically() {
        // "pilot" happens to be named the same on both DTO and entity, but
        // "date" below is the one that only works if the DTO-to-entity
        // sort-field translation (date -> datum) is actually applied.
        Pageable sorted = PageRequest.of(0, 10, Sort.by("pilot").ascending());

        Page<FlightLogEntryDto> result = flightService.findAll(new FlightSearchCriteria(), sorted);

        assertThat(result.getContent().get(0).getPilot()).isEqualTo("Erika Musterfrau");
        assertThat(result.getContent().get(1).getPilot()).isEqualTo("Klaus Kleiner");
    }

    // "aircraftType" (DTO) maps to "muster" (entity) - proves that translation specifically.
    @Test
    void findAll_sortByAircraftTypeAscending_translatesToEntityField() {
        // "aircraftType" on the DTO maps to "muster" on the entity.
        Pageable sorted = PageRequest.of(0, 10, Sort.by("aircraftType").ascending());

        Page<FlightLogEntryDto> result = flightService.findAll(new FlightSearchCriteria(), sorted);

        assertThat(result.getContent().get(0).getAircraftType()).isEqualTo("ASK 21");
    }
}
