package de.windenshelter.flugbuch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import de.windenshelter.flugbuch.configuration.mail.PasswordResetProperties;
import de.windenshelter.flugbuch.model.Pilot;

/**
 * Plain Mockito unit tests for {@link PasswordResetMailService}. Covers the
 * bug found via a real "admin" account: the seeded username "admin" isn't a
 * valid email address, so the mail server rejects it outright unless the
 * pilot's real profile email is preferred instead.
 */
class PasswordResetMailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final PasswordResetProperties properties = new PasswordResetProperties();
    private final PasswordResetMailService service = new PasswordResetMailService(mailSender, properties);

    PasswordResetMailServiceTest() {
        properties.setTokenExpirationMinutes(30);
        properties.setFrontendResetUrl("http://localhost:4200/reset-password");
        properties.setMailFrom("no-reply@flugbuch.local");
    }

    @Test
    void sendResetLink_pilotHasRealEmail_sendsToThatEmailNotUsername() {
        Pilot pilot = new Pilot();
        pilot.setUsername("admin");
        pilot.setEmail("admin@flugbuch.local");

        service.sendResetLink(pilot, "some-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).containsExactly("admin@flugbuch.local");
    }

    @Test
    void sendResetLink_pilotHasNoEmail_fallsBackToUsername() {
        Pilot pilot = new Pilot();
        pilot.setUsername("max.mustermann@edpu.de");
        pilot.setEmail(null);

        service.sendResetLink(pilot, "some-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).containsExactly("max.mustermann@edpu.de");
    }
}
