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
     * Returns, in ONE query, the external_ids from the given set that are
     * already known. Replaces the N+1 pattern of calling
     * {@code existsByExternalId} once per row during a bulk import. Callers
     * should split the input set into bounded-size chunks (bounded IN list).
     */
    @Query("select e.externalId from StagingSchleppbetriebEintrag e where e.externalId in :externalIds")
    List<Integer> findExistingExternalIds(@Param("externalIds") Collection<Integer> externalIds);
}
