package de.windenshelter.flugbuch.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.windenshelter.flugbuch.model.Helper;

/** Data access for confirmed {@link Helper} entries. */
@Repository
public interface HelperRepository extends JpaRepository<Helper, Long> {

    Optional<Helper> findByEmail(String email);
}
