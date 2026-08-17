package de.windenshelter.flugbuch.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Coarse brute-force protection for the three endpoints where repeated
 * guessing is actually dangerous: login (password guessing), register
 * (account-creation spam) and change-password (guessing a pilot's *current*
 * password while already authenticated). Every other endpoint is untouched.
 *
 * <p>Design choice: Bucket4j in pure in-memory mode (a {@link ConcurrentHashMap}
 * of per-client buckets), not Redis or a database table. At the app's
 * current scale - one Spring Boot instance - an external store would only
 * add an operational dependency for no real benefit; buckets simply reset on
 * restart, which is an acceptable trade-off for a rate limit. The moment
 * this runs as more than one instance behind a load balancer, each instance
 * would enforce its own limit independently (e.g. 2 instances effectively
 * double the limit) - fine for now, but the first thing to revisit if the
 * app is ever horizontally scaled.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Package-visible (not private) so tests can drive a bucket to its exact
    // limit without hard-coding a magic number that could silently drift out
    // of sync with the real configuration below.
    static final int LOGIN_CAPACITY = 20;
    static final int REGISTER_CAPACITY = 10;
    static final int CHANGE_PASSWORD_CAPACITY = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    /** One rate-limit rule: which request path it guards, and how many attempts it allows per {@link #WINDOW}. */
    private record RateLimit(String pathSuffix, int capacity) {
    }

    private static final RateLimit[] LIMITS = {
            new RateLimit("/api/v1/auth/login", LOGIN_CAPACITY),
            new RateLimit("/api/v1/auth/register", REGISTER_CAPACITY),
            new RateLimit("/api/v1/pilots/me/password", CHANGE_PASSWORD_CAPACITY),
    };

    // One bucket per (client, endpoint) pair, created lazily on first use.
    // Grows with the number of distinct clients seen; buckets for clients
    // that stop making requests simply stop being touched - no active
    // cleanup needed at this app's scale.
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        RateLimit limit = matchingLimit(request);

        if (limit != null) {
            Bucket bucket = buckets.computeIfAbsent(bucketKey(request, limit), key -> newBucket(limit));
            if (!bucket.tryConsume(1)) {
                // Servlet's HttpServletResponse only defines SC_* constants for
                // the original HTTP/1.1 status codes - 429 (RFC 6585) isn't
                // among them, so this uses Spring's HttpStatus instead.
                response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                        "Too many attempts. Please wait a moment and try again.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private RateLimit matchingLimit(HttpServletRequest request) {
        for (RateLimit limit : LIMITS) {
            if (request.getRequestURI().endsWith(limit.pathSuffix())) {
                return limit;
            }
        }
        return null;
    }

    private String bucketKey(HttpServletRequest request, RateLimit limit) {
        return clientIp(request) + ":" + limit.pathSuffix();
    }

    /**
     * Best-effort client identifier. X-Forwarded-For is trusted here because
     * this app is expected to sit behind a reverse proxy in production (see
     * docker-compose.yml); in local dev, without a proxy in front, the header
     * is simply absent and we fall back to the direct connection's address.
     */
    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Bucket newBucket(RateLimit limit) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit.capacity())
                .refillGreedy(limit.capacity(), WINDOW)
                .build();
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
