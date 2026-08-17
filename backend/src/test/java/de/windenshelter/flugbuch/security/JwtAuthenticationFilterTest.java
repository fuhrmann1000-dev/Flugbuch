package de.windenshelter.flugbuch.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for {@link JwtAuthenticationFilter}. Both collaborators are
 * mocked, so this proves the filter's own decisions - when to authenticate a
 * request, and when to reject a token whose tokenVersion is stale - without
 * needing a database or a running server.
 */
class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain filterChain = mock(FilterChain.class);

    // Every test authenticates (or fails to authenticate) into the same
    // shared static SecurityContextHolder - clear it after each test so
    // results can't leak between them.
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private PilotUserDetails pilotUserDetails(int tokenVersion) {
        return new PilotUserDetails("max.mustermann", "hashed-password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")), tokenVersion);
    }

    @Test
    void doFilter_noAuthorizationHeader_leavesRequestUnauthenticated() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_validTokenMatchingTokenVersion_authenticatesRequest() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("max.mustermann");
        when(jwtService.extractTokenVersion("valid-token")).thenReturn(2);
        when(userDetailsService.loadUserByUsername("max.mustermann")).thenReturn(pilotUserDetails(2));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("max.mustermann");
        verify(filterChain).doFilter(request, response);
    }

    // A token issued before the pilot's most recent password change carries a
    // stale tokenVersion claim - it must be rejected even though it's
    // validly signed and not expired (see PilotService#changePassword).
    @Test
    void doFilter_validTokenStaleTokenVersion_leavesRequestUnauthenticated() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer stale-token");
        when(jwtService.isTokenValid("stale-token")).thenReturn(true);
        when(jwtService.extractUsername("stale-token")).thenReturn("max.mustermann");
        when(jwtService.extractTokenVersion("stale-token")).thenReturn(1);
        when(userDetailsService.loadUserByUsername("max.mustermann")).thenReturn(pilotUserDetails(2));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        // Still passed down the chain unauthenticated - SecurityConfig (not
        // this filter) is what turns "no authentication" into a 401 for
        // protected endpoints.
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_invalidToken_leavesRequestUnauthenticated() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer garbage");
        when(jwtService.isTokenValid("garbage")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
