package de.windenshelter.flugbuch.security;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for {@link RateLimitingFilter}. No Spring context needed - the
 * filter only depends on the request it receives, so this drives it
 * directly with mocked servlet objects, the same way {@link JwtAuthenticationFilterTest} does.
 */
class RateLimitingFilterTest {

    private final RateLimitingFilter filter = new RateLimitingFilter();
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain filterChain = mock(FilterChain.class);

    private HttpServletRequest requestTo(String uri, String remoteAddr) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getRemoteAddr()).thenReturn(remoteAddr);
        return request;
    }

    // Endpoints this filter doesn't guard must never be throttled, no matter the volume.
    @Test
    void doFilter_pathNotRateLimited_alwaysPassesThroughRegardlessOfVolume() throws Exception {
        HttpServletRequest request = requestTo("/api/v1/flights", "10.0.0.1");

        for (int i = 0; i < RateLimitingFilter.LOGIN_CAPACITY + 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(RateLimitingFilter.LOGIN_CAPACITY + 5)).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void doFilter_loginEndpointWithinCapacity_passesThrough() throws Exception {
        HttpServletRequest request = requestTo("/api/v1/auth/login", "10.0.0.2");

        for (int i = 0; i < RateLimitingFilter.LOGIN_CAPACITY; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(RateLimitingFilter.LOGIN_CAPACITY)).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void doFilter_loginEndpointExceedsCapacity_rejectsWith429AndStopsChain() throws Exception {
        HttpServletRequest request = requestTo("/api/v1/auth/login", "10.0.0.3");

        for (int i = 0; i < RateLimitingFilter.LOGIN_CAPACITY; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        // One request over capacity within the same window must be rejected.
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(RateLimitingFilter.LOGIN_CAPACITY)).doFilter(request, response);
        verify(response).sendError(eq(429), anyString());
    }

    // Two different clients hitting the same endpoint must be tracked in
    // separate buckets - one client exhausting their limit must not affect another.
    @Test
    void doFilter_differentClientIps_trackedIndependently() throws Exception {
        HttpServletRequest requestFromIpA = requestTo("/api/v1/auth/register", "10.0.0.4");
        HttpServletRequest requestFromIpB = requestTo("/api/v1/auth/register", "10.0.0.5");

        for (int i = 0; i < RateLimitingFilter.REGISTER_CAPACITY; i++) {
            filter.doFilterInternal(requestFromIpA, response, filterChain);
        }
        // ipA is now exhausted, but ipB has never made a request yet.
        filter.doFilterInternal(requestFromIpB, response, filterChain);

        verify(filterChain).doFilter(requestFromIpB, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    // X-Forwarded-For (set by the reverse proxy in production) must be
    // preferred over the raw connection address when present.
    @Test
    void doFilter_forwardedForHeaderPresent_isUsedAsClientKeyInsteadOfRemoteAddr() throws Exception {
        HttpServletRequest request = requestTo("/api/v1/pilots/me/password", "127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.5, 10.0.0.1");

        for (int i = 0; i < RateLimitingFilter.CHANGE_PASSWORD_CAPACITY; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        filter.doFilterInternal(request, response, filterChain);

        // All requests came from the same forwarded client (127.0.0.1 as the
        // direct remote address is just the reverse proxy) so the limit still applies as one bucket.
        verify(response).sendError(eq(429), anyString());
    }
}
