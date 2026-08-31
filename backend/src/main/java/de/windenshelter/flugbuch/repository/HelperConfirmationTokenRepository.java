package de.windenshelter.flugbuch.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.windenshelter.flugbuch.model.HelperConfirmationToken;

/** Data access for pending {@link HelperConfirmationToken} entries. */
@Repository
public interface HelperConfirmationTokenRepository extends JpaRepository<HelperConfirmationToken, Long> {

    Optional<HelperConfirmationToken> findByToken(String token);
}
