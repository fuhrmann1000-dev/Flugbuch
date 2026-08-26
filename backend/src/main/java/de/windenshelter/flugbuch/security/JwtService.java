package de.windenshelter.flugbuch.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import de.windenshelter.flugbuch.model.Pilot;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/** Issues and validates the JWTs used to authenticate API requests. */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    /**
     * Builds a signed token for {@code pilot}, embedding their email (the
     * login identity), roles and current tokenVersion. Takes the
     * {@link Pilot} entity directly (rather than a Spring Security
     * {@code UserDetails}) because tokenVersion is a Pilot-only concept -
     * see {@link Pilot#getTokenVersion()}.
     */
    public String generateToken(Pilot pilot) {
        Instant now = Instant.now();
        List<String> roles = pilot.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .toList();

        return Jwts.builder()
                .subject(pilot.getEmail())
                .claim("roles", roles)
                .claim("tokenVersion", pilot.getTokenVersion())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.getExpirationMinutes() * 60)))
                .signWith(signingKey())
                .compact();
    }

    /** Returns the email (subject) encoded in a JWT. Only call this after {@link #isTokenValid}. */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Returns the tokenVersion embedded in a JWT at login time. Tokens
     * issued before this field existed carry no such claim - those are
     * treated as version 0, which matches the default value newly-migrated
     * Pilot rows get (see {@link Pilot#getTokenVersion()}), so already-issued
     * tokens keep working across the deploy that introduces this feature.
     * Only call this after {@link #isTokenValid}.
     */
    public int extractTokenVersion(String token) {
        Object claim = parseClaims(token).get("tokenVersion");
        return claim == null ? 0 : ((Number) claim).intValue();
    }

    /** Returns whether {@code token} is well-formed, correctly signed, and not expired. */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
