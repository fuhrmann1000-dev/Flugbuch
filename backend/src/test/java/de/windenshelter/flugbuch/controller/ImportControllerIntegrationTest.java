package de.windenshelter.flugbuch.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.service.MainFlightLogImportService;
import de.windenshelter.flugbuch.service.SchleppbetriebImportException;
import de.windenshelter.flugbuch.service.support.ChunkedDeduplicatingSaver;

/**
 * Integration test for {@link ImportController}. Loads the real security
 * filter chain (via {@code springSecurity()}) to prove the manual CSV
 * upload needs a logged-in pilot but no particular role, matching the
 * default {@code anyRequest().authenticated()} rule in {@code SecurityConfig}.
 */
@SpringBootTest
class ImportControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/imports";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockitoBean
    private MainFlightLogImportService mainFlightLogImportService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    private MockMultipartFile csvFile() {
        return new MockMultipartFile("file", "flugdaten.csv", "text/csv",
                "Datum;Startzeit;...".getBytes());
    }

    @Test
    @DisplayName("POST /api/v1/imports/main-flight-log without a token is rejected with 401")
    void importMainFlightLog_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(multipart(BASE_URL + "/main-flight-log").file(csvFile()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/imports/main-flight-log with a logged-in pilot returns the stored/skipped counts")
    @WithMockUser(roles = "USER")
    void importMainFlightLog_withLoggedInPilot_returnsCounts() throws Exception {
        List<StagingMainFlightLog> parsed = List.of(StagingMainFlightLog.builder().build());
        given(mainFlightLogImportService.importFromStream(any())).willReturn(parsed);
        given(mainFlightLogImportService.importIdempotent(parsed))
                .willReturn(new ChunkedDeduplicatingSaver.Result(3, 1));

        mockMvc.perform(multipart(BASE_URL + "/main-flight-log").file(csvFile()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(3))
                .andExpect(jsonPath("$.skipped").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/imports/main-flight-log with an empty file is rejected with 400")
    @WithMockUser(roles = "USER")
    void importMainFlightLog_withEmptyFile_returnsBadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart(BASE_URL + "/main-flight-log").file(emptyFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/imports/main-flight-log with a malformed CSV is rejected with 400")
    @WithMockUser(roles = "USER")
    void importMainFlightLog_withMalformedCsv_returnsBadRequest() throws Exception {
        willThrow(new SchleppbetriebImportException("Line 2 has 3 columns, expected at least 17."))
                .given(mainFlightLogImportService).importFromStream(any());

        mockMvc.perform(multipart(BASE_URL + "/main-flight-log").file(csvFile()))
                .andExpect(status().isBadRequest());
    }
}
