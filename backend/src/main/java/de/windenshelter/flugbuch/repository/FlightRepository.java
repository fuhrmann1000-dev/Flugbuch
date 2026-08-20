package de.windenshelter.flugbuch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.windenshelter.flugbuch.model.Flight;

/**
 * Persistence for the final, merged {@link Flight} records - distinct from
 * the staging repositories, which only hold raw, not-yet-merged data per
 * import source.
 */
@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
}
