package de.windenshelter.flugbuch.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.windenshelter.flugbuch.dto.FlightLogEntryDto;
import de.windenshelter.flugbuch.dto.FlightSearchCriteria;
import de.windenshelter.flugbuch.dto.FlightType;
import de.windenshelter.flugbuch.service.FlightService;

/**
 * Integration test for {@link FlightController}.
 *
 * Loads the full Spring context and exercises the controller through MockMvc,
 * while the service layer is mocked. Since {@code findAll} now returns a
 * Spring Data {@code Page}, the JSON body is wrapped ({@code content},
 * {@code totalElements}, ...) instead of a bare array.
 *
 * All flight endpoints now require authentication (see {@code SecurityConfig}),
 * so every test here runs as a logged-in user via {@code @WithMockUser}. The
 * "logged out" case is covered separately in
 * {@code FlightControllerSecurityIntegrationTest}, to keep that concern out
 * of these filter/pagination/sorting tests.
 */
@SpringBootTest
@WithMockUser
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
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

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
    @DisplayName("GET /api/v1/flights returns all entries wrapped in a Page")
    void findAll_returnsPage() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].pilot").value("Max Mustermann"))
                .andExpect(jsonPath("$.totalElements").value(1));
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

    // -------------------------------------------------------------------
    // Filtering
    // -------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/flights with no query params binds an all-null criteria")
    void findAll_noParams_bindsEmptyCriteria() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());

        ArgumentCaptor<FlightSearchCriteria> captor = ArgumentCaptor.forClass(FlightSearchCriteria.class);
        verify(flightService).findAll(captor.capture(), any(Pageable.class));
        FlightSearchCriteria criteria = captor.getValue();

        assertThat(criteria.getPilot()).isNull();
        assertThat(criteria.getAircraftType()).isNull();
        assertThat(criteria.getRegistration()).isNull();
        assertThat(criteria.getFlightType()).isNull();
        assertThat(criteria.getDate()).isNull();
        assertThat(criteria.getDateFrom()).isNull();
        assertThat(criteria.getDateTo()).isNull();
    }

    @Test
    @DisplayName("GET /api/v1/flights?pilot=... binds only the pilot field")
    void findAll_filterByPilot_bindsPilotOnly() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL).param("pilot", "Max Mustermann"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].pilot").value("Max Mustermann"));

        ArgumentCaptor<FlightSearchCriteria> captor = ArgumentCaptor.forClass(FlightSearchCriteria.class);
        verify(flightService).findAll(captor.capture(), any(Pageable.class));
        FlightSearchCriteria criteria = captor.getValue();

        assertThat(criteria.getPilot()).isEqualTo("Max Mustermann");
        assertThat(criteria.getAircraftType()).isNull();
        assertThat(criteria.getDate()).isNull();
    }

    @Test
    @DisplayName("GET /api/v1/flights?aircraftType=... binds only the aircraftType field")
    void findAll_filterByAircraftType_bindsAircraftTypeOnly() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL).param("aircraftType", "ASK 21"))
                .andExpect(status().isOk());

        ArgumentCaptor<FlightSearchCriteria> captor = ArgumentCaptor.forClass(FlightSearchCriteria.class);
        verify(flightService).findAll(captor.capture(), any(Pageable.class));
        FlightSearchCriteria criteria = captor.getValue();

        assertThat(criteria.getAircraftType()).isEqualTo("ASK 21");
        assertThat(criteria.getPilot()).isNull();
    }

    @Test
    @DisplayName("GET /api/v1/flights?registration=... binds only the registration field")
    void findAll_filterByRegistration_bindsRegistrationOnly() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL).param("registration", "D-1234"))
                .andExpect(status().isOk());

        ArgumentCaptor<FlightSearchCriteria> captor = ArgumentCaptor.forClass(FlightSearchCriteria.class);
        verify(flightService).findAll(captor.capture(), any(Pageable.class));

        assertThat(captor.getValue().getRegistration()).isEqualTo("D-1234");
    }

    @Test
    @DisplayName("GET /api/v1/flights?flightType=... binds only the flightType field")
    void findAll_filterByFlightType_bindsFlightTypeOnly() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL).param("flightType", "TYPE_1"))
                .andExpect(status().isOk());

        ArgumentCaptor<FlightSearchCriteria> captor = ArgumentCaptor.forClass(FlightSearchCriteria.class);
        verify(flightService).findAll(captor.capture(), any(Pageable.class));

        assertThat(captor.getValue().getFlightType()).isEqualTo(FlightType.TYPE_1);
    }

    @Test
    @DisplayName("GET /api/v1/flights?date=... binds an exact day, e.g. 'today'")
    void findAll_filterByExactDate_bindsDate() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL).param("date", "26.07.2026"))
                .andExpect(status().isOk());

        ArgumentCaptor<FlightSearchCriteria> captor = ArgumentCaptor.forClass(FlightSearchCriteria.class);
        verify(flightService).findAll(captor.capture(), any(Pageable.class));
        FlightSearchCriteria criteria = captor.getValue();

        assertThat(criteria.getDate()).isEqualTo(LocalDate.of(2026, 7, 26));
        assertThat(criteria.getDateFrom()).isNull();
        assertThat(criteria.getDateTo()).isNull();
    }

    @Test
    @DisplayName("GET /api/v1/flights?dateFrom=...&dateTo=... binds a date range")
    void findAll_filterByDateRange_bindsFromAndTo() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL)
                        .param("dateFrom", "01.07.2026")
                        .param("dateTo", "31.07.2026"))
                .andExpect(status().isOk());

        ArgumentCaptor<FlightSearchCriteria> captor = ArgumentCaptor.forClass(FlightSearchCriteria.class);
        verify(flightService).findAll(captor.capture(), any(Pageable.class));
        FlightSearchCriteria criteria = captor.getValue();

        assertThat(criteria.getDateFrom()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(criteria.getDateTo()).isEqualTo(LocalDate.of(2026, 7, 31));
        assertThat(criteria.getDate()).isNull();
    }

    @Test
    @DisplayName("GET /api/v1/flights combines pilot and date filters")
    void findAll_combinedFilters_bindsBoth() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL)
                        .param("pilot", "Max Mustermann")
                        .param("date", "26.07.2026"))
                .andExpect(status().isOk());

        ArgumentCaptor<FlightSearchCriteria> captor = ArgumentCaptor.forClass(FlightSearchCriteria.class);
        verify(flightService).findAll(captor.capture(), any(Pageable.class));
        FlightSearchCriteria criteria = captor.getValue();

        assertThat(criteria.getPilot()).isEqualTo("Max Mustermann");
        assertThat(criteria.getDate()).isEqualTo(LocalDate.of(2026, 7, 26));
    }

    @Test
    @DisplayName("GET /api/v1/flights returns an empty page when no entries match the filter")
    void findAll_noMatches_returnsEmptyPage() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(BASE_URL).param("pilot", "Unknown Pilot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    // -------------------------------------------------------------------
    // Pagination & sorting (query-param binding; the actual sort-field
    // translation is covered end-to-end in FlightServiceFilterIntegrationTest)
    // -------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/flights?page=1&size=5 binds the requested page and size")
    void findAll_paginationParams_bindPageAndSize() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL).param("page", "1").param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(flightService).findAll(any(FlightSearchCriteria.class), captor.capture());
        Pageable pageable = captor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("GET /api/v1/flights?sortBy=DATE&sortDirection=DESC binds the sort order")
    void findAll_sortParams_bindSortOrder() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL).param("sortBy", "DATE").param("sortDirection", "DESC"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(flightService).findAll(any(FlightSearchCriteria.class), captor.capture());
        Sort.Order order = captor.getValue().getSort().getOrderFor("date");

        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("GET /api/v1/flights without sortBy defaults to no particular order")
    void findAll_noSortBy_resultsInUnsortedPageable() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(flightService).findAll(any(FlightSearchCriteria.class), captor.capture());

        assertThat(captor.getValue().getSort().isUnsorted()).isTrue();
    }

    @Test
    @DisplayName("GET /api/v1/flights combines filtering, sorting and pagination")
    void findAll_filterSortAndPaginate_bindsAllThree() throws Exception {
        given(flightService.findAll(any(FlightSearchCriteria.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get(BASE_URL)
                        .param("pilot", "Max Mustermann")
                        .param("sortBy", "PILOT")
                        .param("sortDirection", "ASC")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        ArgumentCaptor<FlightSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(FlightSearchCriteria.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(flightService).findAll(criteriaCaptor.capture(), pageableCaptor.capture());

        assertThat(criteriaCaptor.getValue().getPilot()).isEqualTo("Max Mustermann");
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("pilot").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }
}
