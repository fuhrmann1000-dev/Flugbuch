package de.windenshelter.flugbuch.configuration.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/** Binds the {@code password-reset.*} keys from {@code application.yml}. */
@Data
@Component
@ConfigurationProperties(prefix = "password-reset")
public class PasswordResetProperties {

    private int tokenExpirationMinutes;
    private String frontendResetUrl;
    private String mailFrom;
}
