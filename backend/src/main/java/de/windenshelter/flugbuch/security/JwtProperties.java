package de.windenshelter.flugbuch.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Binds the {@code jwt.*} keys from {@code application.yml}. The signing
 * secret comes from an environment variable in production (see
 * docker-compose.yml); this class only knows the property names.
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long expirationMinutes;
}
