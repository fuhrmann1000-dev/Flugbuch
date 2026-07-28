package de.windenshelter.flugbuch.service;

import java.util.List;

import de.windenshelter.flugbuch.dto.FlightLogEntryDto;
import de.windenshelter.flugbuch.mapper.FlightLogMapper;
import org.springframework.stereotype.Service;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.repository.MainFlightLogStagingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final MainFlightLogStagingRepository stagingRepository;
    private final FlightLogMapper flightLogMapper;

    public List<FlightLogEntryDto> findAll() {
        return stagingRepository.findAll()
                .stream()
                .map(flightLogMapper::toDto)
                .toList();
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
