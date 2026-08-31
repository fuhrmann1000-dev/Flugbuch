package de.windenshelter.flugbuch.configuration.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/** Binds the {@code helper-confirmation.*} keys from {@code application.yml}. */
@Data
@Component
@ConfigurationProperties(prefix = "helper-confirmation")
public class HelperConfirmationProperties {

    private int tokenExpirationMinutes;
    private String frontendConfirmUrl;
    private String mailFrom;
}
