package de.windenshelter.flugbuch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import de.windenshelter.flugbuch.configuration.mail.PasswordResetProperties;
import de.windenshelter.flugbuch.model.PasswordResetToken;
import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.repository.PasswordResetTokenRepository;
import de.windenshelter.flugbuch.repository.PilotRepository;

/**
 * Plain Mockito unit tests for {@link PasswordResetService} - no Spring
 * context or database required.
 */
class PasswordResetServiceTest {

    private final PilotRepository pilotRepository = mock(PilotRepository.class);
    private final PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
    private final PasswordResetMailService mailService = mock(PasswordResetMailService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private final PasswordResetProperties properties = new PasswordResetProperties();
    private final PasswordResetService service;

    PasswordResetServiceTest() {
        properties.setTokenExpirationMinutes(30);
        properties.setFrontendResetUrl("http://localhost:4200/reset-password");
        properties.setMailFrom("no-reply@flugbuch.local");
        service = new PasswordResetService(pilotRepository, tokenRepository, mailService, properties, passwordEncoder);
    }

    private Pilot samplePilot() {
        Pilot pilot = new Pilot();
        pilot.setId(1L);
        pilot.setUsername("Max Mustermann");
        pilot.setEmail("max.mustermann@edpu.de");
        pilot.setPassword("hashed-current-password");
        pilot.setTokenVersion(0);
        return pilot;
    }

    // -------------------------------------------------------------------
    // requestReset
    // -------------------------------------------------------------------

    @Test
    void requestReset_knownEmail_savesTokenAndSendsMail() {
        Pilot pilot = samplePilot();
        when(pilotRepository.findByEmail(pilot.getEmail())).thenReturn(Optional.of(pilot));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.requestReset(pilot.getEmail());

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(mailService).sendResetLink(eq(pilot), anyString());
    }

    @Test
    void requestReset_unknownEmail_doesNothingSilently() {
        when(pilotRepository.findByEmail("nobody@edpu.de")).thenReturn(Optional.empty());

        service.requestReset("nobody@edpu.de");

        verify(tokenRepository, never()).save(any());
        verify(mailService, never()).sendResetLink(any(), anyString());
    }

    @Test
    void requestReset_mailSendFails_tokenIsStillSavedAndNoExceptionEscapes() {
        Pilot pilot = samplePilot();
        when(pilotRepository.findByEmail(pilot.getEmail())).thenReturn(Optional.of(pilot));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new MailSendException("SMTP server unreachable")).when(mailService).sendResetLink(any(), anyString());

        service.requestReset(pilot.getEmail());

        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    // -------------------------------------------------------------------
    // resetPassword
    // -------------------------------------------------------------------

    @Test
    void resetPassword_validToken_updatesPasswordBumpsTokenVersionAndMarksTokenUsed() {
        Pilot pilot = samplePilot();
        PasswordResetToken token = PasswordResetToken.builder()
                .id(1L)
                .pilot(pilot)
                .token("valid-token")
                .expiresAt(Instant.now().plusSeconds(60))
                .used(false)
                .build();
        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPassword1")).thenReturn("hashed-new-password");

        service.resetPassword("valid-token", "NewPassword1");

        assertThat(pilot.getPassword()).isEqualTo("hashed-new-password");
        assertThat(pilot.getTokenVersion()).isEqualTo(1);
        assertThat(token.isUsed()).isTrue();
        verify(pilotRepository).save(pilot);
        verify(tokenRepository).save(token);
    }

    @Test
    void resetPassword_unknownToken_throwsBadRequest() {
        when(tokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword("unknown", "NewPassword1"))
                .isInstanceOf(ResponseStatusException.class);
        verify(pilotRepository, never()).save(any());
    }

    @Test
    void resetPassword_expiredToken_throwsBadRequest() {
        PasswordResetToken token = PasswordResetToken.builder()
                .pilot(samplePilot())
                .token("expired-token")
                .expiresAt(Instant.now().minusSeconds(60))
                .used(false)
                .build();
        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("expired-token", "NewPassword1"))
                .isInstanceOf(ResponseStatusException.class);
        verify(pilotRepository, never()).save(any());
    }

    @Test
    void resetPassword_alreadyUsedToken_throwsBadRequest() {
        PasswordResetToken token = PasswordResetToken.builder()
                .pilot(samplePilot())
                .token("used-token")
                .expiresAt(Instant.now().plusSeconds(60))
                .used(true)
                .build();
        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("used-token", "NewPassword1"))
                .isInstanceOf(ResponseStatusException.class);
        verify(pilotRepository, never()).save(any());
    }
}
