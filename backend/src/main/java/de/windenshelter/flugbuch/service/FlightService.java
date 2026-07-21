package de.windenshelter.flugbuch.service;

import java.util.List;

import org.springframework.stereotype.Service;

import de.windenshelter.flugbuch.dto.FlugbuchEintragDto;
import de.windenshelter.flugbuch.mapper.FlugbuchMapper;
import de.windenshelter.flugbuch.model.StagingSchleppkladdeEintrag;
import de.windenshelter.flugbuch.repository.StagingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlightService {

    private static final String DEFAULT_STATUS = "PENDING";

    private final StagingRepository stagingRepository;
    private final FlugbuchMapper flugbuchMapper;

    public List<FlugbuchEintragDto> findAll() {
        return stagingRepository.findAll()
                .stream()
                .map(flugbuchMapper::toDto)
                .toList();
    }

    public FlugbuchEintragDto findById(Long id) {
        StagingSchleppkladdeEintrag entry = getEntityOrThrow(id);
        return flugbuchMapper.toDto(entry);
    }

    public FlugbuchEintragDto create(FlugbuchEintragDto dto) {
        StagingSchleppkladdeEintrag entity = flugbuchMapper.toEntity(dto);
        entity.setStatus(DEFAULT_STATUS);
        StagingSchleppkladdeEintrag saved = stagingRepository.save(entity);
        return flugbuchMapper.toDto(saved);
    }

    public FlugbuchEintragDto update(Long id, FlugbuchEintragDto dto) {
        StagingSchleppkladdeEintrag existing = getEntityOrThrow(id);
        existing.setFlugDatum(dto.getFlugDatum());
        existing.setKundenNummer(dto.getKundenNummer());
        existing.setNameDesPiloten(dto.getNameDesPiloten());
        StagingSchleppkladdeEintrag saved = stagingRepository.save(existing);
        return flugbuchMapper.toDto(saved);
    }

    public void delete(Long id) {
        StagingSchleppkladdeEintrag existing = getEntityOrThrow(id);
        stagingRepository.delete(existing);
    }

    private StagingSchleppkladdeEintrag getEntityOrThrow(Long id) {
        return stagingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flight entry not found with id " + id));
    }
}
