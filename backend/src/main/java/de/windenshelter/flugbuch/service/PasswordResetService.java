package de.windenshelter.flugbuch.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import de.windenshelter.flugbuch.configuration.mail.PasswordResetProperties;
import de.windenshelter.flugbuch.model.PasswordResetToken;
import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.repository.PasswordResetTokenRepository;
import de.windenshelter.flugbuch.repository.PilotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Business logic behind the "forgot password" flow: requesting a reset link and redeeming it. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PilotRepository pilotRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetMailService passwordResetMailService;
    private final PasswordResetProperties passwordResetProperties;
    private final PasswordEncoder passwordEncoder;

    /**
     * Silently does nothing for an unknown email, so the caller (see
     * {@code AuthController}) can always return the same generic response -
     * revealing whether an account exists would let this endpoint be used to
     * enumerate registered pilots.
     */
    public void requestReset(String email) {
        pilotRepository.findByEmail(email).ifPresent(pilot -> {
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .pilot(pilot)
                    .token(UUID.randomUUID().toString())
                    .expiresAt(Instant.now().plus(Duration.ofMinutes(passwordResetProperties.getTokenExpirationMinutes())))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            try {
                passwordResetMailService.sendResetLink(pilot, resetToken.getToken());
            } catch (MailException e) {
                // Logged, not rethrown: the response to the caller must stay
                // identical either way, for the same reason as above.
                log.error("Failed to send password reset email to {}: {}", pilot.getUsername(), e.getMessage(), e);
            }
        });
    }

    /** Redeems a reset token: sets the new password and invalidates every session issued before now. */
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .filter(t -> !t.isUsed())
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset link"));

        Pilot pilot = resetToken.getPilot();
        pilot.setPassword(passwordEncoder.encode(newPassword));
        pilot.setTokenVersion(pilot.getTokenVersion() + 1);
        pilotRepository.save(pilot);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
