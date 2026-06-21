package de.windenshelter.flugbuch.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.windenshelter.flugbuch.model.StagingSchleppbetriebEintrag;

@Repository
public interface SchleppbetriebStagingRepository extends JpaRepository<StagingSchleppbetriebEintrag, Long> {

    boolean existsByExternalId(Integer externalId);

    /**
     * Liefert in EINER Abfrage die bereits bekannten external_ids aus der
     * uebergebenen Menge. Ersetzt den N+1-Pattern von {@code existsByExternalId}
     * je Zeile beim Massenimport. Aufrufer sollte die Eingabemenge in Chunks
     * begrenzter Groesse aufteilen (gebundene IN-Liste).
     */
    @Query("select e.externalId from StagingSchleppbetriebEintrag e where e.externalId in :externalIds")
    List<Integer> findExistingExternalIds(@Param("externalIds") Collection<Integer> externalIds);
}
