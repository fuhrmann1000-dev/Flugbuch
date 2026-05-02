package de.windenshelter.flugbuch.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.windenshelter.flugbuch.model.StagingSchleppkladdeEintrag;

@Repository
public interface StagingRepository extends JpaRepository<StagingSchleppkladdeEintrag, Long> {
    void deleteByFlugDatumAndKundenNummer(LocalDateTime flugDatum, String kundenNummer);
}