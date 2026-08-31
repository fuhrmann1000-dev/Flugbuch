package de.windenshelter.flugbuch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import de.windenshelter.flugbuch.configuration.mail.HelperConfirmationProperties;
import de.windenshelter.flugbuch.model.CompetitionType;
import de.windenshelter.flugbuch.model.HelperConfirmationToken;

/**
 * Plain Mockito unit tests for {@link HelperConfirmationMailService}. Covers
 * the reason this service exists as its own class distinct from
 * {@link PasswordResetMailService}: create and update need different
 * subject/body text so the recipient knows which one just happened.
 */
class HelperConfirmationMailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final HelperConfirmationProperties properties = new HelperConfirmationProperties();
    private final HelperConfirmationMailService service = new HelperConfirmationMailService(mailSender, properties);

    HelperConfirmationMailServiceTest() {
        properties.setTokenExpirationMinutes(60);
        properties.setFrontendConfirmUrl("http://localhost:4200/helpers/confirm");
        properties.setMailFrom("no-reply@flugbuch.local");
    }

    private HelperConfirmationToken samplePendingToken() {
        return HelperConfirmationToken.builder()
                .token("some-token")
                .firstName("Erika")
                .lastName("Musterfrau")
                .phone("0123456789")
                .email("erika.musterfrau@edpu.de")
                .competition(CompetitionType.PG)
                .skills("radio, retrieval")
                .build();
    }

    @Test
    void sendCreateConfirmation_sendsToSubmittedEmailWithCreateWording() {
        HelperConfirmationToken pendingToken = samplePendingToken();

        service.sendCreateConfirmation(pendingToken);

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("erika.musterfrau@edpu.de");
        assertThat(sent.getSubject()).containsIgnoringCase("sign-up");
        assertThat(sent.getText()).contains("http://localhost:4200/helpers/confirm?token=some-token");
    }

    @Test
    void sendUpdateConfirmation_sendsToSubmittedEmailWithUpdateWording() {
        HelperConfirmationToken pendingToken = samplePendingToken();

        service.sendUpdateConfirmation(pendingToken);

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("erika.musterfrau@edpu.de");
        assertThat(sent.getSubject()).containsIgnoringCase("update");
        assertThat(sent.getText()).contains("http://localhost:4200/helpers/confirm?token=some-token");
    }

    @Test
    void sendCreateConfirmation_andSendUpdateConfirmation_useDifferentSubjectsAndBodies() {
        HelperConfirmationToken pendingToken = samplePendingToken();

        service.sendCreateConfirmation(pendingToken);
        service.sendUpdateConfirmation(pendingToken);

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, org.mockito.Mockito.times(2)).send(captor.capture());
        SimpleMailMessage createMail = captor.getAllValues().get(0);
        SimpleMailMessage updateMail = captor.getAllValues().get(1);

        assertThat(createMail.getSubject()).isNotEqualTo(updateMail.getSubject());
        assertThat(createMail.getText()).isNotEqualTo(updateMail.getText());
    }
}
