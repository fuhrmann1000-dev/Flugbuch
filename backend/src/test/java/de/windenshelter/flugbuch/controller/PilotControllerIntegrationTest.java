package de.windenshelter.flugbuch.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import de.windenshelter.flugbuch.dto.PilotProfileDto;
import de.windenshelter.flugbuch.service.PilotService;

/**
 * Integration test for {@link PilotController}. Loads the full Spring
 * context and exercises the controller through MockMvc with security
 * enabled, while {@link PilotService} is mocked - so this proves routing,
 * request/response mapping and the "who is 'me'" wiring, not the service
 * logic itself (that's {@code PilotServiceTest}).
 */
@SpringBootTest
class PilotControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/pilots/me";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockitoBean
    private PilotService pilotService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("GET /api/v1/pilots/me without a token is rejected with 401")
    void getMyProfile_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "max.mustermann")
    @DisplayName("GET /api/v1/pilots/me returns the authenticated pilot's profile")
    void getMyProfile_authenticated_returnsProfile() throws Exception {
        PilotProfileDto dto = new PilotProfileDto();
        dto.setUsername("max.mustermann");
        dto.setFirstName("Max");
        dto.setHomeAirfield("EDPU — Altes Lager");
        given(pilotService.getMyProfile("max.mustermann")).willReturn(dto);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("max.mustermann"))
                .andExpect(jsonPath("$.firstName").value("Max"));
    }

    @Test
    @WithMockUser(username = "max.mustermann")
    @DisplayName("PUT /api/v1/pilots/me updates the authenticated pilot's own profile")
    void updateMyProfile_authenticated_updatesProfile() throws Exception {
        PilotProfileDto updated = new PilotProfileDto();
        updated.setUsername("max.mustermann");
        updated.setFirstName("Erika");
        given(pilotService.updateMyProfile(eq("max.mustermann"), any())).willReturn(updated);

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Erika\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Erika"));
    }

    @Test
    @WithMockUser(username = "max.mustermann")
    @DisplayName("PUT /api/v1/pilots/me/picture with a valid data URI updates the picture")
    void updateProfilePicture_validRequest_updatesPicture() throws Exception {
        PilotProfileDto updated = new PilotProfileDto();
        updated.setUsername("max.mustermann");
        updated.setProfilePicture("data:image/png;base64,aGVsbG8=");
        given(pilotService.updateProfilePicture(eq("max.mustermann"), any())).willReturn(updated);

        mockMvc.perform(put(BASE_URL + "/picture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profilePicture\":\"data:image/png;base64,aGVsbG8=\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePicture").value("data:image/png;base64,aGVsbG8="));
    }

    @Test
    @WithMockUser(username = "max.mustermann")
    @DisplayName("PUT /api/v1/pilots/me/picture with a non-image-data-URI value is rejected with 400")
    void updateProfilePicture_invalidFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(put(BASE_URL + "/picture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profilePicture\":\"not-a-data-uri\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "max.mustermann")
    @DisplayName("PUT /api/v1/pilots/me/password with a valid request returns 204")
    void changePassword_validRequest_returnsNoContent() throws Exception {
        mockMvc.perform(put(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"newPassword1\"}"))
                .andExpect(status().isNoContent());

        verify(pilotService).changePassword(eq("max.mustermann"), any());
    }

    @Test
    @WithMockUser(username = "max.mustermann")
    @DisplayName("PUT /api/v1/pilots/me/password with the wrong current password surfaces as 400, not 401")
    void changePassword_wrongCurrentPassword_returnsBadRequest() throws Exception {
        willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect"))
                .given(pilotService).changePassword(eq("max.mustermann"), any());

        mockMvc.perform(put(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrongPassword\",\"newPassword\":\"newPassword1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "max.mustermann")
    @DisplayName("PUT /api/v1/pilots/me/password with a new password missing complexity is rejected with 400 before reaching the service")
    void changePassword_newPasswordMissingComplexity_returnsBadRequest() throws Exception {
        mockMvc.perform(put(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"alllowercase1\"}"))
                .andExpect(status().isBadRequest());

        verify(pilotService, never()).changePassword(any(), any());
    }

    @Test
    @WithMockUser(username = "max.mustermann")
    @DisplayName("PUT /api/v1/pilots/me/password with a new password equal to the current one surfaces as 422")
    void changePassword_newPasswordSameAsCurrent_returnsUnprocessableEntity() throws Exception {
        willThrow(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "New password must be different from the current password"))
                .given(pilotService).changePassword(eq("max.mustermann"), any());

        mockMvc.perform(put(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"samePassword1\",\"newPassword\":\"samePassword1\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "max.mustermann")
    @DisplayName("DELETE /api/v1/pilots/me with the correct password returns 204")
    void deleteMyAccount_correctPassword_returnsNoContent() throws Exception {
        mockMvc.perform(delete(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"correctPassword\"}"))
                .andExpect(status().isNoContent());

        verify(pilotService).deleteMyAccount(eq("max.mustermann"), any());
    }

    @Test
    @WithMockUser(username = "max.mustermann")
    @DisplayName("DELETE /api/v1/pilots/me with the wrong password surfaces as 400, not 401")
    void deleteMyAccount_wrongPassword_returnsBadRequest() throws Exception {
        willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is incorrect"))
                .given(pilotService).deleteMyAccount(eq("max.mustermann"), any());

        mockMvc.perform(delete(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"wrongPassword\"}"))
                .andExpect(status().isBadRequest());
    }
}
