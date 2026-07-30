package de.windenshelter.flugbuch.service.ftp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Binds the {@code ftp.*} keys from {@code application.yml}. The actual
 * values come from environment variables in production (see
 * docker-compose.yml); this class only knows the property names.
 */
@Data
@Component
@ConfigurationProperties(prefix = "ftp")
public class FtpProperties {

    private String host;
    private int port;
    private String username;
    private String password;
    private String remoteFilePath;
    private String localDirectory;
}
