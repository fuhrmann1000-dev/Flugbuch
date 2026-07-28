package de.windenshelter.flugbuch.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.windenshelter.flugbuch.dto.FlightLogEntryDto;
import de.windenshelter.flugbuch.service.FlightService;

/**
 * Integration test for {@link FlightController}.
 *
 * Loads the full Spring context and exercises the controller through MockMvc,
 * while the service layer is mocked.
 */
@SpringBootTest
class MainFlightLogControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/flights";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private FlightService flightService;

    private FlightLogEntryDto sampleDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        sampleDto = new FlightLogEntryDto();
        sampleDto.setId(1L);
        sampleDto.setDate(LocalDate.of(2026, 7, 26));
        sampleDto.setStartTime(LocalTime.of(19, 5));
        sampleDto.setLandingTime(LocalTime.of(19, 35));
        sampleDto.setAircraftType("ASK 21");
        sampleDto.setRegistration("D-1234");
        sampleDto.setPilot("Max Mustermann");
        sampleDto.setGuests(0);
        sampleDto.setFlightType("Schulung");
        sampleDto.setDepartureAirfield("EDKA");
        sampleDto.setDestinationAirfield("EDKA");
        sampleDto.setFlightDirector("Erika Musterfrau");
        sampleDto.setTowedAircraft(null);
        sampleDto.setTowHeight(400);
        sampleDto.setAmount(15.0);
        sampleDto.setRemarks("Testflug");
        sampleDto.setFlightCount(1);
    }

    @Test
    @DisplayName("GET /api/v1/flights returns all entries")
    void findAll_returnsList() throws Exception {
        given(flightService.findAll()).willReturn(List.of(sampleDto));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].pilot").value("Max Mustermann"));
    }

    @Test
    @DisplayName("GET /api/v1/flights/{id} returns entry when found")
    void findById_returnsEntry() throws Exception {
        given(flightService.findById(1L)).willReturn(sampleDto);

        mockMvc.perform(get(BASE_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.aircraftType").value("ASK 21"))
                .andExpect(jsonPath("$.registration").value("D-1234"));
    }

    @Test
    @DisplayName("POST /api/v1/flights creates a new entry and returns 201")
    void create_returnsCreated() throws Exception {
        given(flightService.create(any(FlightLogEntryDto.class))).willReturn(sampleDto);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));

        verify(flightService).create(any(FlightLogEntryDto.class));
    }

    @Test
    @DisplayName("PUT /api/v1/flights/{id} updates an existing entry")
    void update_returnsUpdatedEntry() throws Exception {
        given(flightService.update(anyLong(), any(FlightLogEntryDto.class))).willReturn(sampleDto);

        mockMvc.perform(put(BASE_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));

        verify(flightService).update(anyLong(), any(FlightLogEntryDto.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/flights/{id} deletes entry and returns 204")
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(flightService).delete(1L);
    }
}
