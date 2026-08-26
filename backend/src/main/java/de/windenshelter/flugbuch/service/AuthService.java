package de.windenshelter.flugbuch.service;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import de.windenshelter.flugbuch.dto.AuthResponse;
import de.windenshelter.flugbuch.dto.LoginRequest;
import de.windenshelter.flugbuch.dto.RegisterRequest;
import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.model.Role;
import de.windenshelter.flugbuch.repository.PilotRepository;
import de.windenshelter.flugbuch.repository.RoleRepository;
import de.windenshelter.flugbuch.security.JwtService;
import lombok.RequiredArgsConstructor;

/** Business logic behind pilot registration and login. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_ROLE = "USER";

    private final PilotRepository pilotRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /** Creates a new pilot account with the default USER role and a hashed password. Rejects a duplicate email. */
    public void register(RegisterRequest request) {
        if (pilotRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Default role " + DEFAULT_ROLE + " is missing"));

        Pilot pilot = new Pilot();
        pilot.setUsername(request.getUsername());
        pilot.setEmail(request.getEmail());
        pilot.setPassword(passwordEncoder.encode(request.getPassword()));
        pilot.setRoles(Set.of(defaultRole));

        pilotRepository.save(pilot);
    }

    /** Verifies the given credentials and, if they're valid, returns a signed JWT for that pilot. */
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // authenticationManager.authenticate already proved these credentials
        // are correct, so this lookup can't reasonably fail - it's here to
        // hand JwtService the real Pilot entity (for tokenVersion), not to
        // re-check identity.
        Pilot pilot = pilotRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Authenticated pilot vanished mid-request"));
        String token = jwtService.generateToken(pilot);
        return new AuthResponse(token);
    }
}
