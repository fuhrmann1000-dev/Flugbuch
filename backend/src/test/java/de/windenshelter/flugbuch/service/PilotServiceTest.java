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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import de.windenshelter.flugbuch.dto.ChangePasswordRequest;
import de.windenshelter.flugbuch.dto.DeleteAccountRequest;
import de.windenshelter.flugbuch.dto.PilotProfileDto;
import de.windenshelter.flugbuch.dto.UpdatePilotProfileRequest;
import de.windenshelter.flugbuch.dto.UpdateProfilePictureRequest;
import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.repository.PilotRepository;

/**
 * Plain Mockito unit tests for {@link PilotService} - no Spring context or
 * database required. Covers the four operations behind the Profile page:
 * read, update, password change, account deletion.
 */
class PilotServiceTest {

    private final PilotRepository pilotRepository = mock(PilotRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final PilotService pilotService = new PilotService(pilotRepository, passwordEncoder);

    private Pilot samplePilot() {
        Pilot pilot = new Pilot();
        pilot.setId(1L);
        pilot.setUsername("Max Mustermann");
        pilot.setPassword("hashed-current-password");
        pilot.setFirstName("Max");
        pilot.setLastName("Mustermann");
        pilot.setEmail("max.mustermann@edpu.de");
        pilot.setPhone("+49 177 1234567");
        pilot.setLicenseType("PPL(A)");
        pilot.setLicenseNumber("D.PPL(A).12345");
        pilot.setHomeAirfield("EDPU — Altes Lager");
        return pilot;
    }

    // -------------------------------------------------------------------
    // getMyProfile
    // -------------------------------------------------------------------

    @Test
    void getMyProfile_existingPilot_returnsMappedDto() {
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(samplePilot()));

        PilotProfileDto dto = pilotService.getMyProfile("max.mustermann@edpu.de");

        assertThat(dto.getUsername()).isEqualTo("Max Mustermann");
        assertThat(dto.getFirstName()).isEqualTo("Max");
        assertThat(dto.getLastName()).isEqualTo("Mustermann");
        assertThat(dto.getEmail()).isEqualTo("max.mustermann@edpu.de");
        assertThat(dto.getHomeAirfield()).isEqualTo("EDPU — Altes Lager");
    }

    @Test
    void getMyProfile_unknownEmail_throwsNotFound() {
        when(pilotRepository.findByEmail("ghost@edpu.de")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pilotService.getMyProfile("ghost@edpu.de"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    // -------------------------------------------------------------------
    // updateMyProfile
    // -------------------------------------------------------------------

    @Test
    void updateMyProfile_validRequest_overwritesProfileFieldsAndSaves() {
        Pilot existing = samplePilot();
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(existing));
        when(pilotRepository.save(any(Pilot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePilotProfileRequest request = new UpdatePilotProfileRequest();
        request.setFirstName("Erika");
        request.setLastName("Musterfrau");
        request.setEmail("max.mustermann@edpu.de");
        request.setPhone("+49 177 9999999");
        request.setLicenseType("CPL(A)");
        request.setLicenseNumber("D.CPL(A).99999");
        request.setHomeAirfield("EDKA — Kamenz");

        PilotProfileDto result = pilotService.updateMyProfile("max.mustermann@edpu.de", request);

        assertThat(result.getFirstName()).isEqualTo("Erika");
        assertThat(result.getLastName()).isEqualTo("Musterfrau");
        assertThat(result.getHomeAirfield()).isEqualTo("EDKA — Kamenz");
        // Display name must never be touched by this endpoint.
        assertThat(result.getUsername()).isEqualTo("Max Mustermann");
        verify(pilotRepository).save(existing);
    }

    @Test
    void updateMyProfile_emailUnchanged_doesNotCheckForDuplicates() {
        Pilot existing = samplePilot();
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(existing));
        when(pilotRepository.save(any(Pilot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePilotProfileRequest request = new UpdatePilotProfileRequest();
        request.setEmail("max.mustermann@edpu.de");
        request.setHomeAirfield("EDKA — Kamenz");

        pilotService.updateMyProfile("max.mustermann@edpu.de", request);

        verify(pilotRepository, never()).existsByEmail(any());
    }

    // Email is now the unique login identity - changing it to one another
    // pilot already has must be rejected, not left to fail as a raw DB
    // constraint violation.
    @Test
    void updateMyProfile_newEmailAlreadyTaken_throwsConflictAndDoesNotSave() {
        Pilot existing = samplePilot();
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(existing));
        when(pilotRepository.existsByEmail("erika@edpu.de")).thenReturn(true);

        UpdatePilotProfileRequest request = new UpdatePilotProfileRequest();
        request.setEmail("erika@edpu.de");
        request.setHomeAirfield("EDKA — Kamenz");

        assertThatThrownBy(() -> pilotService.updateMyProfile("max.mustermann@edpu.de", request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");

        verify(pilotRepository, never()).save(any());
    }

    // -------------------------------------------------------------------
    // updateProfilePicture
    // -------------------------------------------------------------------

    @Test
    void updateProfilePicture_validRequest_overwritesPictureAndSaves() {
        Pilot existing = samplePilot();
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(existing));
        when(pilotRepository.save(any(Pilot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfilePictureRequest request = new UpdateProfilePictureRequest();
        request.setProfilePicture("data:image/png;base64,aGVsbG8=");

        PilotProfileDto result = pilotService.updateProfilePicture("max.mustermann@edpu.de", request);

        assertThat(result.getProfilePicture()).isEqualTo("data:image/png;base64,aGVsbG8=");
        // Everything else must be untouched by this endpoint.
        assertThat(result.getFirstName()).isEqualTo("Max");
        verify(pilotRepository).save(existing);
    }

    // -------------------------------------------------------------------
    // changePassword
    // -------------------------------------------------------------------

    @Test
    void changePassword_correctCurrentPassword_hashesAndSavesNewPassword() {
        Pilot existing = samplePilot();
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("oldPassword", "hashed-current-password")).thenReturn(true);
        when(passwordEncoder.encode("newPassword1")).thenReturn("hashed-new-password");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword1");

        pilotService.changePassword("max.mustermann@edpu.de", request);

        assertThat(existing.getPassword()).isEqualTo("hashed-new-password");
        verify(pilotRepository).save(existing);
    }

    // A successful password change must invalidate any token issued before
    // it, so an old, possibly-leaked session can't keep being used.
    @Test
    void changePassword_correctCurrentPassword_incrementsTokenVersion() {
        Pilot existing = samplePilot();
        existing.setTokenVersion(2);
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("oldPassword", "hashed-current-password")).thenReturn(true);
        when(passwordEncoder.encode("newPassword1")).thenReturn("hashed-new-password");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword1");

        pilotService.changePassword("max.mustermann@edpu.de", request);

        assertThat(existing.getTokenVersion()).isEqualTo(3);
    }

    // Wrong current password must be a 400, not a 401 - a 401 here would make the
    // frontend's auth interceptor treat it as an expired session and force a logout.
    @Test
    void changePassword_wrongCurrentPassword_throwsBadRequestAndDoesNotSave() {
        Pilot existing = samplePilot();
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrongPassword", "hashed-current-password")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword1");

        assertThatThrownBy(() -> pilotService.changePassword("max.mustermann@edpu.de", request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

        verify(pilotRepository, never()).save(any());
    }

    // A new password identical to the current one is a distinct failure mode
    // from "wrong current password" - it must surface as 422, not 400, so the
    // frontend can show a message that actually matches what went wrong.
    @Test
    void changePassword_newPasswordSameAsCurrentPassword_throwsUnprocessableEntityAndDoesNotSave() {
        Pilot existing = samplePilot();
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("samePassword1", "hashed-current-password")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("samePassword1");
        request.setNewPassword("samePassword1");

        assertThatThrownBy(() -> pilotService.changePassword("max.mustermann@edpu.de", request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("422");

        verify(pilotRepository, never()).save(any());
    }

    // -------------------------------------------------------------------
    // deleteMyAccount
    // -------------------------------------------------------------------

    @Test
    void deleteMyAccount_correctPassword_deletesPilot() {
        Pilot existing = samplePilot();
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("correctPassword", "hashed-current-password")).thenReturn(true);

        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword("correctPassword");

        pilotService.deleteMyAccount("max.mustermann@edpu.de", request);

        verify(pilotRepository).delete(existing);
    }

    @Test
    void deleteMyAccount_wrongPassword_throwsBadRequestAndDoesNotDelete() {
        Pilot existing = samplePilot();
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrongPassword", "hashed-current-password")).thenReturn(false);

        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword("wrongPassword");

        assertThatThrownBy(() -> pilotService.deleteMyAccount("max.mustermann@edpu.de", request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

        verify(pilotRepository, never()).delete(any(Pilot.class));
    }
}
