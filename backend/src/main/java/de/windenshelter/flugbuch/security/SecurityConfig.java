package de.windenshelter.flugbuch.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Wires up stateless, JWT-based authentication: {@code /api/v1/auth/**},
 * Swagger and the public parts of the helper sign-up (ticket #54) stay
 * public, everything else requires a valid Bearer token - and the
 * full-detail helper listing additionally requires the ADMIN role.
 * {@link CustomUserDetailsService} + {@link #passwordEncoder()} are picked
 * up automatically by Spring Security to build the {@link AuthenticationManager}.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;

    /**
     * The origin the Angular frontend is served from. Defaults to the local
     * dev server; override with the {@code CORS_ALLOWED_ORIGIN} environment
     * variable once the frontend has a real production URL.
     */
    @Value("${cors.allowed-origin:http://localhost:4200}")
    private String allowedOrigin;

    /** BCrypt is the standard, salted, one-way hash for storing pilot passwords. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Exposes the AuthenticationManager so {@code AuthService} can verify login credentials. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Tells the browser it's fine for {@link #allowedOrigin} (the Angular
     * dev server) to call this API. Without this, the browser blocks every
     * cross-origin request itself before it even reaches our code - a valid
     * JWT wouldn't matter, the request never leaves the browser.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /** Defines which routes are public and installs the JWT filter ahead of Spring's default login filter. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        // Helper sign-up (ticket #54): reachable without a login, same as
                        // /api/v1/auth/** above - registering/editing/viewing the reduced
                        // helper list must not require a Flugbuch pilot account. The
                        // full-detail listing (plain GET /api/v1/helpers, no extra path
                        // segment) stays ADMIN-only.
                        .requestMatchers(HttpMethod.POST, "/api/v1/helpers/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/helpers/confirm").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/helpers/public").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/helpers").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // Without this, Spring Security's default behaviour for a missing/invalid
                // token is 403 Forbidden (it treats the request as an anonymous user that
                // isn't allowed in, not as "not authenticated"). We want the more correct
                // REST semantics: no/bad token -> 401, valid token but wrong role -> 403.
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        (request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                // jwtAuthenticationFilter must be registered first: addFilterBefore
                // needs its "before" argument to already have a known position in
                // the chain, and JwtAuthenticationFilter only gets one once this
                // line runs (UsernamePasswordAuthenticationFilter's position is
                // already known to Spring Security out of the box). Registering
                // rateLimitingFilter relative to it before that happens fails at
                // startup with "does not have a registered order".
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Rate limiting runs before JWT auth so a client hammering
                // /auth/login or /auth/register - endpoints that never carry
                // a token in the first place - gets rejected before doing
                // any real work at all.
                .addFilterBefore(rateLimitingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
