package de.windenshelter.flugbuch.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * Wires up stateless, JWT-based authentication: {@code /api/v1/auth/**} and
 * Swagger stay public, everything else requires a valid Bearer token.
 * {@link CustomUserDetailsService} + {@link #passwordEncoder()} are picked
 * up automatically by Spring Security to build the {@link AuthenticationManager}.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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
                        .anyRequest().authenticated())
                // Without this, Spring Security's default behaviour for a missing/invalid
                // token is 403 Forbidden (it treats the request as an anonymous user that
                // isn't allowed in, not as "not authenticated"). We want the more correct
                // REST semantics: no/bad token -> 401, valid token but wrong role -> 403.
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        (request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
