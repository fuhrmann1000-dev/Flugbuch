package de.windenshelter.flugbuch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import de.windenshelter.flugbuch.dto.ExportRange;
import de.windenshelter.flugbuch.dto.FlightLogEntryDto;
import de.windenshelter.flugbuch.dto.FlightSearchCriteria;
import de.windenshelter.flugbuch.service.FlightExportService.ExportRangeInfo;

/**
 * Plain Mockito unit tests for {@link FlightExportService}: the date-range
 * resolution (ALL/YEAR/MONTH) and the CSV serialization, including the
 * quoting rules that must mirror what {@code CsvLineParser} expects to read
 * back on import.
 */
class FlightExportServiceTest {

    private final FlightService flightService = mock(FlightService.class);
    private final FlightExportService service = new FlightExportService(flightService);

    private FlightLogEntryDto sampleFlight() {
        FlightLogEntryDto dto = new FlightLogEntryDto();
        dto.setDate(LocalDate.of(2026, 8, 18));
        dto.setStartTime(LocalTime.of(9, 30));
        dto.setLandingTime(LocalTime.of(9, 58));
        dto.setAircraftType("ASK 21");
        dto.setRegistration("D-1234");
        dto.setPilot("Max Mustermann");
        dto.setGuests(0);
        dto.setFlightType("Schulung");
        dto.setDepartureAirfield("EDKA");
        dto.setDestinationAirfield("EDKA");
        dto.setFlightDirector("Erika Musterfrau");
        dto.setFlightCount(1);
        return dto;
    }

    // -------------------------------------------------------------------
    // resolveRange
    // -------------------------------------------------------------------

    @Test
    void resolveRange_all_leavesDateBoundsUnset() {
        ExportRangeInfo info = service.resolveRange(ExportRange.ALL);

        assertThat(info.criteria().getDateFrom()).isNull();
        assertThat(info.criteria().getDateTo()).isNull();
        assertThat(info.title()).isEqualTo("Flugbuch - Gesamtauszug");
    }

    @Test
    void resolveRange_year_coversJanuaryFirstToDecemberThirtyFirst() {
        LocalDate today = LocalDate.now();

        ExportRangeInfo info = service.resolveRange(ExportRange.YEAR);

        assertThat(info.criteria().getDateFrom()).isEqualTo(today.withDayOfYear(1));
        assertThat(info.criteria().getDateTo()).isEqualTo(today.withDayOfYear(today.lengthOfYear()));
        assertThat(info.title()).contains(String.valueOf(today.getYear()));
    }

    @Test
    void resolveRange_month_coversFirstToLastDayOfCurrentMonth() {
        LocalDate today = LocalDate.now();

        ExportRangeInfo info = service.resolveRange(ExportRange.MONTH);

        assertThat(info.criteria().getDateFrom()).isEqualTo(today.withDayOfMonth(1));
        assertThat(info.criteria().getDateTo()).isEqualTo(today.withDayOfMonth(today.lengthOfMonth()));
    }

    // -------------------------------------------------------------------
    // toCsv
    // -------------------------------------------------------------------

    @Test
    void toCsv_startsWithUtf8BomAndTheGermanHeaderRow() {
        when(flightService.findAll(any(FlightSearchCriteria.class), any())).thenReturn(Page.empty());

        byte[] csv = service.toCsv(new FlightSearchCriteria());

        assertThat(csv[0]).isEqualTo((byte) 0xEF);
        assertThat(csv[1]).isEqualTo((byte) 0xBB);
        assertThat(csv[2]).isEqualTo((byte) 0xBF);
        String text = new String(csv, 3, csv.length - 3, StandardCharsets.UTF_8);
        assertThat(text).startsWith("Datum;Startzeit;Landezeit;Muster;Kennzeichen;Pilot;Gäste;Flugart;"
                + "Startplatz;Zielplatz;Flugleiter;Geschleppter;Schlepphöhe;Betrag;Bemerkung;Fluganzahl\r\n");
    }

    @Test
    void toCsv_writesOneSemicolonSeparatedRowPerFlight() {
        Page<FlightLogEntryDto> page = new PageImpl<>(List.of(sampleFlight()));
        when(flightService.findAll(any(FlightSearchCriteria.class), any())).thenReturn(page);

        byte[] csv = service.toCsv(new FlightSearchCriteria());
        String text = new String(csv, 3, csv.length - 3, StandardCharsets.UTF_8);
        String[] lines = text.split("\r\n");

        assertThat(lines).hasSize(2);
        assertThat(lines[1]).isEqualTo(
                "18.08.2026;09:30;09:58;ASK 21;D-1234;Max Mustermann;0;Schulung;EDKA;EDKA;Erika Musterfrau;;;;;1");
    }

    @Test
    void toCsv_quotesFieldsContainingTheDelimiter() {
        FlightLogEntryDto flight = sampleFlight();
        flight.setRemarks("Landung; kurz vor Regen");
        Page<FlightLogEntryDto> page = new PageImpl<>(List.of(flight));
        when(flightService.findAll(any(FlightSearchCriteria.class), any())).thenReturn(page);

        byte[] csv = service.toCsv(new FlightSearchCriteria());
        String text = new String(csv, 3, csv.length - 3, StandardCharsets.UTF_8);

        assertThat(text).contains("\"Landung; kurz vor Regen\"");
    }
}
