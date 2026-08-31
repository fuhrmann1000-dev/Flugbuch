package de.windenshelter.flugbuch.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import de.windenshelter.flugbuch.configuration.mail.HelperConfirmationProperties;
import de.windenshelter.flugbuch.model.HelperConfirmationToken;
import lombok.RequiredArgsConstructor;

/**
 * Sends the confirmation link for a pending helper registration. Two
 * distinct templates - one for a brand-new sign-up, one for an update to an
 * already-known helper - so the recipient can tell which of the two just
 * happened from the subject line alone, without having to click through.
 */
@Service
@RequiredArgsConstructor
public class HelperConfirmationMailService {

    private final JavaMailSender mailSender;
    private final HelperConfirmationProperties helperConfirmationProperties;

    public void sendCreateConfirmation(HelperConfirmationToken pendingToken) {
        send(pendingToken,
                "Confirm your Flugbuch helper sign-up",
                """
                Hi %s,

                Thanks for signing up as a helper for the Flatlands competition! \
                Click the link below to confirm your registration. This link expires \
                in %d minutes and hasn't been applied yet - nothing has been saved \
                until you confirm.

                %s

                If you didn't request this, you can safely ignore this email.
                """);
    }

    public void sendUpdateConfirmation(HelperConfirmationToken pendingToken) {
        send(pendingToken,
                "Confirm your Flugbuch helper details update",
                """
                Hi %s,

                Someone submitted updated helper details for the Flatlands competition \
                using this email address. Click the link below to confirm and apply \
                the changes. This link expires in %d minutes and hasn't been applied \
                yet - your existing details are unchanged until you confirm.

                %s

                If you didn't request this, you can safely ignore this email - your \
                existing details will stay exactly as they are.
                """);
    }

    private void send(HelperConfirmationToken pendingToken, String subject, String bodyTemplate) {
        String confirmLink = helperConfirmationProperties.getFrontendConfirmUrl() + "?token=" + pendingToken.getToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(helperConfirmationProperties.getMailFrom());
        message.setTo(pendingToken.getEmail());
        message.setSubject(subject);
        message.setText(bodyTemplate.formatted(
                pendingToken.getFirstName(),
                helperConfirmationProperties.getTokenExpirationMinutes(),
                confirmLink));

        mailSender.send(message);
    }
}
