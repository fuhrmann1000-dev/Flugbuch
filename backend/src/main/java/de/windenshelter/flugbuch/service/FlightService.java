package de.windenshelter.flugbuch.service;

import de.windenshelter.flugbuch.dto.FlightLogEntryDto;
import de.windenshelter.flugbuch.dto.FlightSearchCriteria;
import de.windenshelter.flugbuch.mapper.FlightLogMapper;
import de.windenshelter.flugbuch.repository.specification.FlightSortMapping;
import de.windenshelter.flugbuch.repository.specification.FlightSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.repository.MainFlightLogStagingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/** Business logic for flight log entries: CRUD plus filtered/sorted/paginated listing. */
@Service
@RequiredArgsConstructor
public class FlightService {

    private final MainFlightLogStagingRepository stagingRepository;
    private final FlightLogMapper flightLogMapper;

    /**
     * Returns flight log entries, paginated and sorted per {@code pageable},
     * narrowed down by whichever fields are set on {@code criteria}. Sort
     * properties are given using the public DTO's field names (e.g.
     * {@code date}, {@code aircraftType}) and translated internally to the
     * entity's field names; see {@link FlightSortMapping}.
     */
    public Page<FlightLogEntryDto> findAll(FlightSearchCriteria criteria, Pageable pageable) {
        Specification<StagingMainFlightLog> spec = FlightSpecifications.fromCriteria(criteria);
        Pageable entityPageable = FlightSortMapping.toEntitySort(pageable);
        return stagingRepository.findAll(spec, entityPageable)
                .map(flightLogMapper::toDto);
    }

    public FlightLogEntryDto findById(Long id) {
        StagingMainFlightLog entry = getEntityOrThrow(id);
        return flightLogMapper.toDto(entry);
    }

    public FlightLogEntryDto create(FlightLogEntryDto dto) {
        StagingMainFlightLog entity = flightLogMapper.toEntity(dto);
        StagingMainFlightLog saved = stagingRepository.save(entity);
        return flightLogMapper.toDto(saved);
    }

    public FlightLogEntryDto update(Long id, FlightLogEntryDto dto) {
        StagingMainFlightLog existing = getEntityOrThrow(id);
        existing.setDatum(dto.getDate());
        existing.setStartzeit(dto.getStartTime());
        existing.setLandezeit(dto.getLandingTime());
        existing.setMuster(dto.getAircraftType());
        existing.setKennzeichen(dto.getRegistration());
        existing.setPilot(dto.getPilot());
        existing.setGaeste(dto.getGuests());
        existing.setFlugart(dto.getFlightType());
        existing.setStartPlatz(dto.getDepartureAirfield());
        existing.setZielPlatz(dto.getDestinationAirfield());
        existing.setFlugLeiter(dto.getFlightDirector());
        existing.setGeschleppter(dto.getTowedAircraft());
        existing.setSchleppHoehe(dto.getTowHeight());
        existing.setBetrag(dto.getAmount());
        existing.setBemerkung(dto.getRemarks());
        existing.setFlugAnzahl(dto.getFlightCount());
        StagingMainFlightLog saved = stagingRepository.save(existing);
        return flightLogMapper.toDto(saved);
    }

    public void delete(Long id) {
        StagingMainFlightLog existing = getEntityOrThrow(id);
        stagingRepository.delete(existing);
    }

    private StagingMainFlightLog getEntityOrThrow(Long id) {
        return stagingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flight entry not found with id " + id));
    }
}
