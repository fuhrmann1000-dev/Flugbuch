package de.windenshelter.flugbuch.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import de.windenshelter.flugbuch.dto.ChangePasswordRequest;
import de.windenshelter.flugbuch.dto.DeleteAccountRequest;
import de.windenshelter.flugbuch.dto.PilotProfileDto;
import de.windenshelter.flugbuch.dto.UpdatePilotProfileRequest;
import de.windenshelter.flugbuch.dto.UpdateProfilePictureRequest;
import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.repository.PilotRepository;
import lombok.RequiredArgsConstructor;

/**
 * Business logic behind the Profile page: reading/updating the logged-in
 * pilot's own profile, changing their password, and deleting their account.
 * Every method here operates on "the pilot behind the current request" -
 * the username comes from the JWT (see {@code PilotController}), never from
 * a path variable, so a pilot can only ever act on their own account.
 */
@Service
@RequiredArgsConstructor
public class PilotService {

    private final PilotRepository pilotRepository;
    private final PasswordEncoder passwordEncoder;

    public PilotProfileDto getMyProfile(String username) {
        return toDto(findByUsername(username));
    }

    public PilotProfileDto updateMyProfile(String username, UpdatePilotProfileRequest request) {
        Pilot pilot = findByUsername(username);

        pilot.setFirstName(request.getFirstName());
        pilot.setLastName(request.getLastName());
        pilot.setEmail(request.getEmail());
        pilot.setPhone(request.getPhone());
        pilot.setLicenseType(request.getLicenseType());
        pilot.setLicenseNumber(request.getLicenseNumber());
        pilot.setHomeAirfield(request.getHomeAirfield());

        return toDto(pilotRepository.save(pilot));
    }

    /**
     * Verifies the current password before setting the new one. A wrong
     * current password is reported as 400, not 401 - a 401 anywhere outside
     * {@code /auth/**} makes the frontend's auth interceptor log the pilot
     * out and redirect to /login, which would be wrong here: the pilot *is*
     * validly authenticated, they just mistyped their current password.
     */
    public void changePassword(String username, ChangePasswordRequest request) {
        Pilot pilot = findByUsername(username);
        verifyPassword(request.getCurrentPassword(), pilot, "Current password is incorrect");

        pilot.setPassword(passwordEncoder.encode(request.getNewPassword()));
        pilotRepository.save(pilot);
    }

    /** Overwrites the pilot's avatar image. The whole data URI is validated by {@link UpdateProfilePictureRequest}. */
    public PilotProfileDto updateProfilePicture(String username, UpdateProfilePictureRequest request) {
        Pilot pilot = findByUsername(username);
        pilot.setProfilePicture(request.getProfilePicture());
        return toDto(pilotRepository.save(pilot));
    }

    /** Same 400-not-401 reasoning as {@link #changePassword} applies to the confirmation password here. */
    public void deleteMyAccount(String username, DeleteAccountRequest request) {
        Pilot pilot = findByUsername(username);
        verifyPassword(request.getPassword(), pilot, "Password is incorrect");

        pilotRepository.delete(pilot);
    }

    private Pilot findByUsername(String username) {
        return pilotRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pilot not found"));
    }

    /**
     * Shared by {@link #changePassword} and {@link #deleteMyAccount} - both
     * need the pilot to re-prove they know their own password before doing
     * something sensitive. Throws 400 (not 401) on a mismatch; see the
     * {@code changePassword} javadoc for why that distinction matters here.
     */
    private void verifyPassword(String rawPassword, Pilot pilot, String errorMessage) {
        if (!passwordEncoder.matches(rawPassword, pilot.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    private PilotProfileDto toDto(Pilot pilot) {
        PilotProfileDto dto = new PilotProfileDto();
        dto.setId(pilot.getId());
        dto.setUsername(pilot.getUsername());
        dto.setFirstName(pilot.getFirstName());
        dto.setLastName(pilot.getLastName());
        dto.setEmail(pilot.getEmail());
        dto.setPhone(pilot.getPhone());
        dto.setLicenseType(pilot.getLicenseType());
        dto.setLicenseNumber(pilot.getLicenseNumber());
        dto.setHomeAirfield(pilot.getHomeAirfield());
        dto.setProfilePicture(pilot.getProfilePicture());
        return dto;
    }
}
