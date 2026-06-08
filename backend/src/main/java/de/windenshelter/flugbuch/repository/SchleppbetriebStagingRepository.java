package de.windenshelter.flugbuch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.windenshelter.flugbuch.model.StagingSchleppbetriebEintrag;

@Repository
public interface SchleppbetriebStagingRepository extends JpaRepository<StagingSchleppbetriebEintrag, Long> {

    boolean existsByExternalId(Integer externalId);
}
