package de.windenshelter.flugbuch.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Unit tests for {@link JwtService}. No Spring context needed - JwtService
 * only depends on a plain {@link JwtProperties} instance, which is exactly
 * the kind of thing the boss assumed would need a real server to test.
 */
class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-at-least-32-bytes-long-0123456789";

    private JwtProperties jwtProperties;
    private JwtService jwtService;
    private Pilot pilot;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setExpirationMinutes(60);
        jwtService = new JwtService(jwtProperties);

        pilot = new Pilot();
        pilot.setUsername("max.mustermann");
        pilot.setRoles(Set.of(Role.builder().name("USER").build()));
        pilot.setTokenVersion(0);
    }

    // A freshly generated token must be accepted as valid.
    @Test
    void generateToken_thenIsTokenValid_returnsTrue() {
        String token = jwtService.generateToken(pilot);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    // The username embedded at generation time must come back out unchanged.
    @Test
    void generateToken_thenExtractUsername_returnsOriginalUsername() {
        String token = jwtService.generateToken(pilot);

        assertThat(jwtService.extractUsername(token)).isEqualTo("max.mustermann");
    }

    // The pilot's tokenVersion at login time must come back out unchanged -
    // this is what JwtAuthenticationFilter compares against the database on
    // every later request to detect an invalidated (e.g. password-changed) session.
    @Test
    void generateToken_thenExtractTokenVersion_returnsOriginalTokenVersion() {
        pilot.setTokenVersion(3);

        String token = jwtService.generateToken(pilot);

        assertThat(jwtService.extractTokenVersion(token)).isEqualTo(3);
    }

    // Tokens issued before tokenVersion existed carry no such claim; they must
    // be treated as version 0 (the default a migrated Pilot row gets) rather
    // than blowing up, so nobody is forced to re-login the moment this
    // feature is deployed.
    @Test
    void extractTokenVersion_tokenWithoutClaim_returnsZero() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String legacyToken = Jwts.builder()
                .subject("max.mustermann")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();

        assertThat(jwtService.extractTokenVersion(legacyToken)).isZero();
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

        String tokenSignedByOther = otherJwtService.generateToken(pilot);

        assertThat(jwtService.isTokenValid(tokenSignedByOther)).isFalse();
    }

    // A token whose expiration is already in the past must be rejected.
    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        jwtProperties.setExpirationMinutes(-1);
        String expiredToken = jwtService.generateToken(pilot);

        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
    }
}
