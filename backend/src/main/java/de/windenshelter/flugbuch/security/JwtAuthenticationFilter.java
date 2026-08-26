package de.windenshelter.flugbuch.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Runs once per request: reads the {@code Authorization: Bearer <token>}
 * header and, if it carries a valid JWT, authenticates the request for the
 * rest of the filter chain. Requests without a valid token are simply passed
 * through unauthenticated - {@link SecurityConfig} decides afterwards
 * whether the target endpoint requires authentication.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (jwtService.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = jwtService.extractEmail(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Design note (why tokenVersion, not a denylist table or a switch
            // to refresh tokens): CustomUserDetailsService already has to load
            // this Pilot row from the database on every single authenticated
            // request just to build `userDetails` above, so reading the
            // pilot's *current* tokenVersion here is free - no extra query,
            // no extra table to grow/prune, no new infrastructure (e.g. Redis)
            // needed. A mismatch means the token was issued before the
            // pilot's most recent password change (see
            // PilotService#changePassword, which increments tokenVersion),
            // so we treat it exactly like an invalid/expired token: leave the
            // SecurityContext unauthenticated and let the request fall
            // through to SecurityConfig's normal 401 handling for protected
            // endpoints. That 401 is intentional here (unlike the 400s used
            // for a mistyped password elsewhere) - the session backing this
            // token is genuinely dead, so the frontend's auth interceptor
            // logging the pilot out and sending them to /login is correct.
            boolean tokenVersionMatches = !(userDetails instanceof PilotUserDetails pilotUserDetails)
                    || jwtService.extractTokenVersion(token) == pilotUserDetails.getTokenVersion();

            if (tokenVersionMatches) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
