package de.windenshelter.flugbuch.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import de.windenshelter.flugbuch.configuration.mail.PasswordResetProperties;
import de.windenshelter.flugbuch.model.Pilot;
import lombok.RequiredArgsConstructor;

/** Sends the "forgot password" reset link by email. */
@Service
@RequiredArgsConstructor
public class PasswordResetMailService {

    private final JavaMailSender mailSender;
    private final PasswordResetProperties passwordResetProperties;

    public void sendResetLink(Pilot pilot, String token) {
        String resetLink = passwordResetProperties.getFrontendResetUrl() + "?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(passwordResetProperties.getMailFrom());
        message.setTo(recipientAddress(pilot));
        message.setSubject("Reset your Flugbuch password");
        message.setText("""
                Hi,

                Someone requested a password reset for your Flugbuch account. \
                If this was you, click the link below to choose a new password. \
                This link expires in %d minutes.

                %s

                If you didn't request this, you can safely ignore this email.
                """.formatted(passwordResetProperties.getTokenExpirationMinutes(), resetLink));

        mailSender.send(message);
    }

    /**
     * Prefers the pilot's real profile email (see {@code Pilot.email}) when
     * it's set, since it's an actual delivery address. Username is only used
     * as a fallback for pilots who registered with an email-shaped username
     * but never filled in their profile - it's not guaranteed to be a real
     * address (e.g. the seeded "admin" account, whose username is just
     * "admin"), so preferring the real email avoids sends that the mail
     * server rejects outright.
     */
    private String recipientAddress(Pilot pilot) {
        String email = pilot.getEmail();
        return (email != null && !email.isBlank()) ? email : pilot.getUsername();
    }
}
