package de.windenshelter.flugbuch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.windenshelter.flugbuch.dto.ChangePasswordRequest;
import de.windenshelter.flugbuch.dto.DeleteAccountRequest;
import de.windenshelter.flugbuch.dto.PilotProfileDto;
import de.windenshelter.flugbuch.dto.UpdatePilotProfileRequest;
import de.windenshelter.flugbuch.dto.UpdateProfilePictureRequest;
import de.windenshelter.flugbuch.service.PilotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST endpoints behind the Profile page. Every route here acts on "me" -
 * the pilot identified by the request's JWT ({@code authentication.getName()})
 * - there is no "look up any pilot by id" endpoint; that's a separate,
 * admin-only concern this ticket doesn't cover.
 */
@RestController
@RequestMapping("/api/v1/pilots")
@RequiredArgsConstructor
public class PilotController {

    private final PilotService pilotService;

    @GetMapping("/me")
    public PilotProfileDto getMyProfile(Authentication authentication) {
        return pilotService.getMyProfile(authentication.getName());
    }

    @PutMapping("/me")
    public PilotProfileDto updateMyProfile(Authentication authentication,
            @Valid @RequestBody UpdatePilotProfileRequest request) {
        return pilotService.updateMyProfile(authentication.getName(), request);
    }

    @PutMapping("/me/picture")
    public PilotProfileDto updateProfilePicture(Authentication authentication,
            @Valid @RequestBody UpdateProfilePictureRequest request) {
        return pilotService.updateProfilePicture(authentication.getName(), request);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        pilotService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication,
            @Valid @RequestBody DeleteAccountRequest request) {
        pilotService.deleteMyAccount(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
