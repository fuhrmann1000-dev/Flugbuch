package de.windenshelter.flugbuch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import de.windenshelter.flugbuch.dto.AuthResponse;
import de.windenshelter.flugbuch.dto.LoginRequest;
import de.windenshelter.flugbuch.dto.RegisterRequest;
import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.model.Role;
import de.windenshelter.flugbuch.repository.PilotRepository;
import de.windenshelter.flugbuch.repository.RoleRepository;
import de.windenshelter.flugbuch.security.CustomUserDetailsService;
import de.windenshelter.flugbuch.security.JwtService;

/**
 * Plain Mockito unit tests for {@link AuthService}. Every collaborator
 * (repositories, password hashing, the Spring Security AuthenticationManager,
 * JWT generation) is mocked, so this proves the registration/login
 * orchestration works without a database or a running server.
 */
class AuthServiceTest {

    private final PilotRepository pilotRepository = mock(PilotRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
    private final JwtService jwtService = mock(JwtService.class);

    private final AuthService authService = new AuthService(pilotRepository, roleRepository, passwordEncoder,
            authenticationManager, userDetailsService, jwtService);

    // Happy path: new username, password gets hashed, default USER role gets assigned.
    @Test
    void register_newUsername_hashesPasswordAndSavesWithDefaultRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new.pilot");
        request.setPassword("plainTextPassword");

        when(pilotRepository.existsByUsername("new.pilot")).thenReturn(false);
        Role userRole = Role.builder().id(1L).name("USER").build();
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("plainTextPassword")).thenReturn("hashed-password");

        authService.register(request);

        ArgumentCaptor<Pilot> pilotCaptor = ArgumentCaptor.forClass(Pilot.class);
        verify(pilotRepository).save(pilotCaptor.capture());
        Pilot savedPilot = pilotCaptor.getValue();

        assertThat(savedPilot.getUsername()).isEqualTo("new.pilot");
        assertThat(savedPilot.getPassword()).isEqualTo("hashed-password");
        assertThat(savedPilot.getRoles()).containsExactly(userRole);
    }

    // Registering an already-taken username must be rejected before touching the password encoder.
    @Test
    void register_duplicateUsername_throwsConflict() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing.pilot");
        request.setPassword("somePassword");
        when(pilotRepository.existsByUsername("existing.pilot")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");

        verify(passwordEncoder, never()).encode(any());
        verify(pilotRepository, never()).save(any());
    }

    // Happy path: correct credentials produce a signed token.
    @Test
    void login_validCredentials_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("max.mustermann");
        request.setPassword("correctPassword");

        UserDetails userDetails = User.withUsername("max.mustermann").password("hash").authorities("ROLE_USER").build();
        when(userDetailsService.loadUserByUsername("max.mustermann")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("signed-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("signed-jwt-token");
    }

    // Wrong credentials must surface as 401, not leak a stack trace or a 500.
    @Test
    void login_invalidCredentials_throwsUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setUsername("max.mustermann");
        request.setPassword("wrongPassword");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");

        verify(jwtService, never()).generateToken(any());
    }
}
