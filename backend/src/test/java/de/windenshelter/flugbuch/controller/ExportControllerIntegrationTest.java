package de.windenshelter.flugbuch.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import de.windenshelter.flugbuch.dto.ExportRange;
import de.windenshelter.flugbuch.dto.FlightSearchCriteria;
import de.windenshelter.flugbuch.service.DailyFlightLogPrintService;
import de.windenshelter.flugbuch.service.FlightExportService;
import de.windenshelter.flugbuch.service.FlightExportService.ExportRangeInfo;

/**
 * Integration test for {@link ExportController}. Loads the real security
 * filter chain to prove the export endpoints need a logged-in pilot but no
 * particular role, matching {@code SecurityConfig}'s default
 * {@code anyRequest().authenticated()}.
 */
@SpringBootTest
class ExportControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/exports";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockitoBean
    private FlightExportService flightExportService;

    @MockitoBean
    private DailyFlightLogPrintService dailyFlightLogPrintService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("GET /api/v1/exports/csv without a token is rejected with 401")
    void exportCsv_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/csv"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/exports/csv with a logged-in pilot returns a CSV attachment")
    @WithMockUser(roles = "USER")
    void exportCsv_withLoggedInPilot_returnsCsvAttachment() throws Exception {
        ExportRangeInfo info = new ExportRangeInfo(new FlightSearchCriteria(), "Flugbuch - Gesamtauszug", "Alle Einträge");
        given(flightExportService.resolveRange(ExportRange.ALL)).willReturn(info);
        given(flightExportService.toCsv(any())).willReturn("Datum;...\r\n".getBytes());

        mockMvc.perform(get(BASE_URL + "/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")));
    }

    @Test
    @DisplayName("GET /api/v1/exports/pdf with a logged-in pilot returns a PDF attachment")
    @WithMockUser(roles = "USER")
    void exportPdf_withLoggedInPilot_returnsPdfAttachment() throws Exception {
        ExportRangeInfo info = new ExportRangeInfo(new FlightSearchCriteria(), "Flugbuch - Jahresauszug 2026",
                "Zeitraum: 01.01.2026 - 31.12.2026");
        given(flightExportService.resolveRange(ExportRange.YEAR)).willReturn(info);
        given(dailyFlightLogPrintService.generateExportPdf(any(), any(), any())).willReturn(new byte[] {'%', 'P', 'D', 'F'});

        mockMvc.perform(get(BASE_URL + "/pdf").param("range", "YEAR"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @DisplayName("GET /api/v1/exports/pdf without a token is rejected with 401")
    void exportPdf_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/pdf"))
                .andExpect(status().isUnauthorized());
    }
}
