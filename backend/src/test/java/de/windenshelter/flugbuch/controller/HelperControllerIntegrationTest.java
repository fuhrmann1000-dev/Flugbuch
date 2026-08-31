package de.windenshelter.flugbuch.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.windenshelter.flugbuch.dto.HelperAdminDto;
import de.windenshelter.flugbuch.dto.HelperPublicDto;
import de.windenshelter.flugbuch.dto.HelperRegistrationRequest;
import de.windenshelter.flugbuch.model.CompetitionType;
import de.windenshelter.flugbuch.service.HelperService;

/**
 * Integration test for {@link HelperController}. Loads the full Spring
 * context (including the real security filter chain, via
 * {@code springSecurity()}) to prove the access split described in ticket
 * #54: register/confirm/public are reachable without a token, the full
 * listing needs ADMIN.
 */
@SpringBootTest
class HelperControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/helpers";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private HelperService helperService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    private HelperRegistrationRequest sampleRequest() {
        HelperRegistrationRequest request = new HelperRegistrationRequest();
        request.setFirstName("Erika");
        request.setLastName("Musterfrau");
        request.setPhone("0123456789");
        request.setEmail("erika.musterfrau@edpu.de");
        request.setCompetition(CompetitionType.PG);
        request.setSkills("radio, retrieval");
        request.setAvailableDays(Set.of(DayOfWeek.SATURDAY));
        return request;
    }

    @Test
    @DisplayName("POST /api/v1/helpers/register without a token still succeeds (public)")
    void register_withoutToken_returnsAccepted() throws Exception {
        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("POST /api/v1/helpers/register with a blank email is rejected before hitting the service")
    void register_blankEmail_returnsBadRequest() throws Exception {
        HelperRegistrationRequest request = sampleRequest();
        request.setEmail("");

        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/helpers/confirm without a token (auth) still succeeds (public)")
    void confirm_withoutAuthToken_returnsOk() throws Exception {
        mockMvc.perform(get(BASE_URL + "/confirm").param("token", "some-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/helpers/public without a token still succeeds (public)")
    void getPublicList_withoutToken_returnsOk() throws Exception {
        given(helperService.getPublicList()).willReturn(List.of(new HelperPublicDto()));

        mockMvc.perform(get(BASE_URL + "/public"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/helpers without any token is rejected with 401")
    void getAdminList_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/helpers with a logged-in non-admin pilot is rejected with 403")
    @WithMockUser(roles = "USER")
    void getAdminList_withNonAdminUser_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/helpers with an ADMIN pilot succeeds")
    @WithMockUser(roles = "ADMIN")
    void getAdminList_withAdminUser_returnsOk() throws Exception {
        given(helperService.getAdminList()).willReturn(List.of(new HelperAdminDto()));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }
}
