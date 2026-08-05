package de.windenshelter.flugbuch.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.windenshelter.flugbuch.dto.AuthResponse;
import de.windenshelter.flugbuch.dto.LoginRequest;
import de.windenshelter.flugbuch.dto.RegisterRequest;
import de.windenshelter.flugbuch.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Public endpoints for pilot registration and login; both are reachable without a token. */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Creates a new pilot account with the default USER role. */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** Validates credentials and returns a signed JWT to use as a Bearer token on subsequent requests. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
