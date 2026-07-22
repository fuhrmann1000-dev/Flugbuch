package de.windenshelter.flugbuch.repository;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface MainFlightLogStagingRepository extends JpaRepository<StagingMainFlightLog, Long> {

    /**
     * Grobe Vorfilterung: liefert vorhandene Eintraege, deren Kennzeichen UND
     * Datum jeweils in den uebergebenen Mengen enthalten sind. JPQL kann keine
     * Tupel-IN-Klausel (Datum, Startzeit, Kennzeichen) ausdruecken, daher muss
     * der Aufrufer die exakte Ubereinstimmung zusaetzlich in Java pruefen.
     */
    @Query("select f from StagingMainFlightLog f where f.kennzeichen in :kennzeichen and f.datum in :daten")
    List<StagingMainFlightLog> findByLicensePlateInAndDateIn(
            @Param("kennzeichen") Collection<String> kennzeichen,
            @Param("daten") Collection<LocalDate> daten);
}