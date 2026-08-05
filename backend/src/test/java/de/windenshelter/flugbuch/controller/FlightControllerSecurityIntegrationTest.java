package de.windenshelter.flugbuch.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import de.windenshelter.flugbuch.dto.FlightLogEntryDto;
import de.windenshelter.flugbuch.dto.FlightSearchCriteria;
import de.windenshelter.flugbuch.service.FlightService;

/**
 * Proves that {@code /api/v1/flights} actually enforces authentication now
 * (ticket: token-based auth). Kept separate from
 * {@code MainFlightLogControllerIntegrationTest} so that file can stay
 * focused on filtering/sorting/pagination behaviour and just assume an
 * already-authenticated caller.
 */
@SpringBootTest
class FlightControllerSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockitoBean
    private FlightService flightService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("GET /api/v1/flights without a token is rejected with 401")
    void findAll_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/flights"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/flights with a logged-in user succeeds")
    @WithMockUser
    void findAll_withAuthenticatedUser_returnsOk() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(new FlightLogEntryDto())));

        mockMvc.perform(get("/api/v1/flights"))
                .andExpect(status().isOk());
    }
}
