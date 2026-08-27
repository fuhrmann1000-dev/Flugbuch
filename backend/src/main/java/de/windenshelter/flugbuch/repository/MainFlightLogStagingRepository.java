package de.windenshelter.flugbuch.repository;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface MainFlightLogStagingRepository extends JpaRepository<StagingMainFlightLog, Long>,
        JpaSpecificationExecutor<StagingMainFlightLog> {

    /**
     * Rough pre-filter: returns existing entries whose registration AND date
     * are each contained in the given sets. JPQL can't express a tuple IN
     * clause (date, startTime, registration), so the caller must additionally
     * check the exact match in Java.
     *
     * "or f.kennzeichen is null" is intentional, not accidental: SQL/JPQL's
     * "IN" never matches a NULL column, not even against a NULL inside the
     * given set (three-valued logic). Without this clause, an already-stored
     * entry with no registration (e.g. an unregistered free-flight aircraft)
     * would look "unknown" on every import run and get duplicated - exactly
     * the symptom reported in the duplicates ticket.
     */
    @Query("select f from StagingMainFlightLog f where (f.kennzeichen in :kennzeichen or f.kennzeichen is null) and f.datum in :daten")
    List<StagingMainFlightLog> findByLicensePlateInAndDateIn(
            @Param("kennzeichen") Collection<String> kennzeichen,
            @Param("daten") Collection<LocalDate> daten);
}