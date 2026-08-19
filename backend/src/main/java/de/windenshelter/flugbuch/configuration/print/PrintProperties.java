package de.windenshelter.flugbuch.configuration.print;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Binds the {@code print.*} keys from {@code application.yml}. The actual
 * output directory comes from an environment variable in production (see
 * docker-compose.yml); this class only knows the property name.
 */
@Data
@Component
@ConfigurationProperties(prefix = "print")
public class PrintProperties {

    private String outputDirectory;
}
