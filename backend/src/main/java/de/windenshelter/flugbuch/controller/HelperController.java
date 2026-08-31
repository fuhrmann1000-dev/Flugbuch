package de.windenshelter.flugbuch.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.windenshelter.flugbuch.dto.HelperAdminDto;
import de.windenshelter.flugbuch.dto.HelperPublicDto;
import de.windenshelter.flugbuch.dto.HelperRegistrationRequest;
import de.windenshelter.flugbuch.service.HelperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints for the "Flatlands" competition helper sign-up (ticket #54).
 * {@code /register}, {@code /confirm} and {@code /public} are reachable
 * without a login, same as {@code /api/v1/auth/**}; only the full-detail
 * {@code GET /api/v1/helpers} is ADMIN-only. See {@code SecurityConfig} for
 * where that split is actually enforced.
 */
@RestController
@RequestMapping("/api/v1/helpers")
@RequiredArgsConstructor
public class HelperController {

    private final HelperService helperService;

    /**
     * Single create-or-update form. Nothing is saved yet - a confirmation
     * email is sent, and the change only takes effect once its link is
     * clicked (see {@link #confirm}). Always returns 202 regardless of
     * whether this turns out to be a new helper or an update to an existing
     * one, since that decision only happens inside the service.
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody HelperRegistrationRequest request) {
        helperService.submitRegistration(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /** Redeems the confirmation link: creates or updates the helper row. */
    @GetMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestParam String token) {
        helperService.confirm(token);
        return ResponseEntity.ok().build();
    }

    /** Public, reduced listing: first name, competition, skills, availability - no contact details. */
    @GetMapping("/public")
    public List<HelperPublicDto> getPublicList() {
        return helperService.getPublicList();
    }

    /** Full listing including contact details - ADMIN only. */
    @GetMapping
    public List<HelperAdminDto> getAdminList() {
        return helperService.getAdminList();
    }
}
