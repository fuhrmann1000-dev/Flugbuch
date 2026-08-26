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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import de.windenshelter.flugbuch.dto.AuthResponse;
import de.windenshelter.flugbuch.dto.LoginRequest;
import de.windenshelter.flugbuch.dto.RegisterRequest;
import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.model.Role;
import de.windenshelter.flugbuch.repository.PilotRepository;
import de.windenshelter.flugbuch.repository.RoleRepository;
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
    private final JwtService jwtService = mock(JwtService.class);

    private final AuthService authService = new AuthService(pilotRepository, roleRepository, passwordEncoder,
            authenticationManager, jwtService);

    // Happy path: new email, password gets hashed, default USER role gets assigned.
    @Test
    void register_newEmail_hashesPasswordAndSavesWithDefaultRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("New Pilot");
        request.setEmail("new.pilot@edpu.de");
        request.setPassword("plainTextPassword");

        when(pilotRepository.existsByEmail("new.pilot@edpu.de")).thenReturn(false);
        Role userRole = Role.builder().id(1L).name("USER").build();
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("plainTextPassword")).thenReturn("hashed-password");

        authService.register(request);

        ArgumentCaptor<Pilot> pilotCaptor = ArgumentCaptor.forClass(Pilot.class);
        verify(pilotRepository).save(pilotCaptor.capture());
        Pilot savedPilot = pilotCaptor.getValue();

        assertThat(savedPilot.getUsername()).isEqualTo("New Pilot");
        assertThat(savedPilot.getEmail()).isEqualTo("new.pilot@edpu.de");
        assertThat(savedPilot.getPassword()).isEqualTo("hashed-password");
        assertThat(savedPilot.getRoles()).containsExactly(userRole);
    }

    // Registering an already-taken email must be rejected before touching the password encoder.
    @Test
    void register_duplicateEmail_throwsConflict() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("Existing Pilot");
        request.setEmail("existing.pilot@edpu.de");
        request.setPassword("somePassword");
        when(pilotRepository.existsByEmail("existing.pilot@edpu.de")).thenReturn(true);

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
        request.setEmail("max.mustermann@edpu.de");
        request.setPassword("correctPassword");

        Pilot pilot = new Pilot();
        pilot.setUsername("max.mustermann");
        pilot.setEmail("max.mustermann@edpu.de");
        pilot.setPassword("hash");
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(pilot));
        when(jwtService.generateToken(pilot)).thenReturn("signed-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("signed-jwt-token");
    }

    // Wrong credentials must surface as 401, not leak a stack trace or a 500.
    @Test
    void login_invalidCredentials_throwsUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setEmail("max.mustermann@edpu.de");
        request.setPassword("wrongPassword");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");

        verify(jwtService, never()).generateToken(any());
    }
}
