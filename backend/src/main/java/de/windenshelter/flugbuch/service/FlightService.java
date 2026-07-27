package de.windenshelter.flugbuch.service;

import java.util.List;

import org.springframework.stereotype.Service;

import de.windenshelter.flugbuch.dto.FlugbuchEintragDto;
import de.windenshelter.flugbuch.mapper.FlugbuchMapper;
import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.repository.MainFlightLogStagingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final MainFlightLogStagingRepository stagingRepository;
    private final FlugbuchMapper flugbuchMapper;

    public List<FlugbuchEintragDto> findAll() {
        return stagingRepository.findAll()
                .stream()
                .map(flugbuchMapper::toDto)
                .toList();
    }

    public FlugbuchEintragDto findById(Long id) {
        StagingMainFlightLog entry = getEntityOrThrow(id);
        return flugbuchMapper.toDto(entry);
    }

    public FlugbuchEintragDto create(FlugbuchEintragDto dto) {
        StagingMainFlightLog entity = flugbuchMapper.toEntity(dto);
        StagingMainFlightLog saved = stagingRepository.save(entity);
        return flugbuchMapper.toDto(saved);
    }

    public FlugbuchEintragDto update(Long id, FlugbuchEintragDto dto) {
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
        return flugbuchMapper.toDto(saved);
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
