package de.windenshelter.flugbuch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.windenshelter.flugbuch.model.StagingWinchkladdeEintrag;

@Repository
public interface WinchkladdeStagingRepository extends JpaRepository<StagingWinchkladdeEintrag, Long> {

    boolean existsByExternalId(Integer externalId);
}
