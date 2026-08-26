package de.windenshelter.flugbuch.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.windenshelter.flugbuch.dto.AuthResponse;
import de.windenshelter.flugbuch.dto.LoginRequest;
import de.windenshelter.flugbuch.dto.RegisterRequest;
import de.windenshelter.flugbuch.service.AuthService;

/**
 * Integration test for {@link AuthController}. Loads the full Spring context
 * (including the real security filter chain, via {@code springSecurity()})
 * to prove that {@code /api/v1/auth/**} is genuinely reachable without a
 * token, while the actual register/login logic is mocked out.
 */
@SpringBootTest
class AuthControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/auth";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with valid data returns 201, no token required")
    void register_validRequest_returnsCreated() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("New Pilot");
        request.setEmail("new.pilot@edpu.de");
        request.setPassword("aLongEnoughPassword");

        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with a blank email is rejected before hitting the service")
    void register_blankEmail_returnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("New Pilot");
        request.setEmail("");
        request.setPassword("aLongEnoughPassword");

        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with an email already taken returns 409")
    void register_duplicateEmail_returnsConflict() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("Existing Pilot");
        request.setEmail("existing.pilot@edpu.de");
        request.setPassword("aLongEnoughPassword");
        willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered"))
                .given(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with valid credentials returns 200 and a token")
    void login_validCredentials_returnsToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("max.mustermann@edpu.de");
        request.setPassword("correctPassword");
        given(authService.login(any(LoginRequest.class))).willReturn(new AuthResponse("signed-jwt-token"));

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("signed-jwt-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with wrong credentials returns 401")
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("max.mustermann@edpu.de");
        request.setPassword("wrongPassword");
        given(authService.login(any(LoginRequest.class)))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
