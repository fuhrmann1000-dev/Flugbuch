package de.windenshelter.flugbuch.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.windenshelter.flugbuch.model.Pilot;

/** Data access for {@link Pilot} accounts. */
@Repository
public interface PilotRepository extends JpaRepository<Pilot, Long> {

    Optional<Pilot> findByEmail(String email);

    boolean existsByEmail(String email);
}
