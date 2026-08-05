package de.windenshelter.flugbuch.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Unit tests for {@link JwtService}. No Spring context needed - JwtService
 * only depends on a plain {@link JwtProperties} instance, which is exactly
 * the kind of thing the boss assumed would need a real server to test.
 */
class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-at-least-32-bytes-long-0123456789";

    private JwtProperties jwtProperties;
    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setExpirationMinutes(60);
        jwtService = new JwtService(jwtProperties);

        userDetails = User.withUsername("max.mustermann")
                .password("irrelevant-for-this-test")
                .authorities("ROLE_USER")
                .build();
    }

    // A freshly generated token must be accepted as valid.
    @Test
    void generateToken_thenIsTokenValid_returnsTrue() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    // The username embedded at generation time must come back out unchanged.
    @Test
    void generateToken_thenExtractUsername_returnsOriginalUsername() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("max.mustermann");
    }

    // Garbage input must never be treated as a valid token.
    @Test
    void isTokenValid_malformedToken_returnsFalse() {
        assertThat(jwtService.isTokenValid("this-is-not-a-jwt")).isFalse();
    }

    // A token signed with a different secret must be rejected, even if otherwise well-formed.
    @Test
    void isTokenValid_wrongSignature_returnsFalse() {
        JwtProperties otherProperties = new JwtProperties();
        otherProperties.setSecret("a-completely-different-secret-key-32-bytes-or-more!!");
        otherProperties.setExpirationMinutes(60);
        JwtService otherJwtService = new JwtService(otherProperties);

        String tokenSignedByOther = otherJwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(tokenSignedByOther)).isFalse();
    }

    // A token whose expiration is already in the past must be rejected.
    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        jwtProperties.setExpirationMinutes(-1);
        String expiredToken = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
    }
}
