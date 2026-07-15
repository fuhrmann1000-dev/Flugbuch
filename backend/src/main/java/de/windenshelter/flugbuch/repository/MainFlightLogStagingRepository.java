package de.windenshelter.flugbuch.repository;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.model.StagingSchleppbetriebEintrag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MainFlightLogStagingRepository extends JpaRepository<StagingMainFlightLog, Long> {


}
