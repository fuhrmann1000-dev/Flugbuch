package de.windenshelter.flugbuch.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Binds the {@code admin.*} keys from {@code application.yml}: the
 * credentials {@link DefaultAdminSeeder} uses for the always-present admin
 * account. The real password comes from an environment variable in
 * production (see docker-compose.yml); this class only knows the property
 * names.
 */
@Data
@Component
@ConfigurationProperties(prefix = "admin")
public class AdminProperties {

    private String username;
    private String password;
}
