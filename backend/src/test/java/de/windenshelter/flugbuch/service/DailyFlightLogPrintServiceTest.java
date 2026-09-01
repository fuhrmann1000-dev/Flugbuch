package de.windenshelter.flugbuch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import de.windenshelter.flugbuch.configuration.print.PrintProperties;
import de.windenshelter.flugbuch.dto.FlightLogEntryDto;
import de.windenshelter.flugbuch.dto.FlightSearchCriteria;
import de.windenshelter.flugbuch.dto.PrintableFlightRow;

/**
 * Plain Mockito unit tests for {@link DailyFlightLogPrintService} - no Spring
 * context or database required. {@link FlightService} and Thymeleaf's
 * {@link TemplateEngine} are mocked, since their own behaviour is already
 * covered elsewhere; what's tested here is the row-formatting logic and that
 * a real PDF file ends up on disk (OpenHTMLtoPDF itself is not mocked, so
 * this also proves the builder is wired up correctly).
 */
class DailyFlightLogPrintServiceTest {

    private final FlightService flightService = mock(FlightService.class);
    private final TemplateEngine templateEngine = mock(TemplateEngine.class);
    private final PrintProperties printProperties = new PrintProperties();

    private DailyFlightLogPrintService service;

    @TempDir
    Path tempDir;

    private void initService() {
        printProperties.setOutputDirectory(tempDir.toString());
        service = new DailyFlightLogPrintService(flightService, templateEngine, printProperties);
    }

    private FlightLogEntryDto sampleFlight() {
        FlightLogEntryDto dto = new FlightLogEntryDto();
        dto.setDate(LocalDate.of(2026, 8, 18));
        dto.setStartTime(LocalTime.of(9, 30));
        dto.setLandingTime(LocalTime.of(9, 58));
        dto.setAircraftType("ASK 21");
        dto.setRegistration("D-1234");
        dto.setPilot("Max Mustermann");
        dto.setGuests(0);
        dto.setFlightType("Schulung");
        dto.setDepartureAirfield("EDKA");
        dto.setDestinationAirfield("EDKA");
        dto.setFlightDirector("Erika Musterfrau");
        dto.setTowedAircraft(null);
        dto.setTowHeight(null);
        dto.setAmount(null);
        dto.setRemarks(null);
        dto.setFlightCount(1);
        return dto;
    }

    // -------------------------------------------------------------------
    // generateDailyPrint
    // -------------------------------------------------------------------

    @Test
    void generateDailyPrint_withFlights_writesValidPdfNamedAfterTheDate() throws IOException {
        initService();
        LocalDate date = LocalDate.of(2026, 8, 18);
        Page<FlightLogEntryDto> page = new PageImpl<>(List.of(sampleFlight()));
        when(flightService.findAll(any(FlightSearchCriteria.class), eq(Pageable.unpaged()))).thenReturn(page);
        when(templateEngine.process(eq("flight-log-print"), any(IContext.class)))
                .thenReturn("<html><body><h1>Flugbuch</h1></body></html>");

        Path result = service.generateDailyPrint(date);

        assertThat(result.getFileName().toString()).isEqualTo("flugbuch-2026-08-18.pdf");
        assertThat(Files.exists(result)).isTrue();
        byte[] bytes = Files.readAllBytes(result);
        assertThat(new String(bytes, 0, 4, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF");
    }

    @Test
    void generateDailyPrint_noFlights_stillWritesAPdf() throws IOException {
        initService();
        LocalDate date = LocalDate.of(2026, 8, 19);
        when(flightService.findAll(any(FlightSearchCriteria.class), eq(Pageable.unpaged())))
                .thenReturn(Page.empty());
        when(templateEngine.process(eq("flight-log-print"), any(IContext.class)))
                .thenReturn("<html><body>Keine Flüge an diesem Tag.</body></html>");

        Path result = service.generateDailyPrint(date);

        assertThat(Files.exists(result)).isTrue();
        byte[] bytes = Files.readAllBytes(result);
        assertThat(new String(bytes, 0, 4, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF");
    }

    @Test
    void generateDailyPrint_passesFormattedRowsAndDateToTheTemplate() throws IOException {
        initService();
        LocalDate date = LocalDate.of(2026, 8, 18);
        Page<FlightLogEntryDto> page = new PageImpl<>(List.of(sampleFlight()));
        when(flightService.findAll(any(FlightSearchCriteria.class), eq(Pageable.unpaged()))).thenReturn(page);
        when(templateEngine.process(eq("flight-log-print"), any(IContext.class)))
                .thenReturn("<html><body>irrelevant for this test</body></html>");

        service.generateDailyPrint(date);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        org.mockito.Mockito.verify(templateEngine).process(eq("flight-log-print"), contextCaptor.capture());
        Context context = contextCaptor.getValue();

        assertThat(context.getVariable("title")).isEqualTo("Flugbuch - Tagesauszug");
        assertThat(context.getVariable("subtitle")).isEqualTo("18.08.2026");

        @SuppressWarnings("unchecked")
        List<PrintableFlightRow> rows = (List<PrintableFlightRow>) context.getVariable("rows");
        assertThat(rows).hasSize(1);
        PrintableFlightRow row = rows.get(0);
        assertThat(row.getTimeRange()).isEqualTo("09:30 - 09:58");
        assertThat(row.getAircraftType()).isEqualTo("ASK 21");
        assertThat(row.getRegistration()).isEqualTo("D-1234");
        assertThat(row.getPilot()).isEqualTo("Max Mustermann");
        assertThat(row.getGuests()).isEqualTo("0");
        assertThat(row.getRoute()).isEqualTo("EDKA -> EDKA");
        // Fields left null on the DTO (towedAircraft, amount, remarks, ...) must
        // become the "-" placeholder rather than the literal string "null".
        assertThat(row.getTowedAircraft()).isEqualTo("-");
        assertThat(row.getAmount()).isEqualTo("-");
        assertThat(row.getRemarks()).isEqualTo("-");
    }

    @Test
    void generateDailyPrint_createsOutputDirectoryIfMissing() throws IOException {
        printProperties.setOutputDirectory(tempDir.resolve("nested/print-dir").toString());
        service = new DailyFlightLogPrintService(flightService, templateEngine, printProperties);
        when(flightService.findAll(any(FlightSearchCriteria.class), eq(Pageable.unpaged())))
                .thenReturn(Page.empty());
        when(templateEngine.process(eq("flight-log-print"), any(IContext.class)))
                .thenReturn("<html><body>empty</body></html>");

        Path result = service.generateDailyPrint(LocalDate.of(2026, 8, 20));

        assertThat(Files.exists(result)).isTrue();
    }

    // -------------------------------------------------------------------
    // generateExportPdf
    // -------------------------------------------------------------------

    @Test
    void generateExportPdf_returnsValidPdfBytes() throws IOException {
        initService();
        Page<FlightLogEntryDto> page = new PageImpl<>(List.of(sampleFlight()));
        when(flightService.findAll(any(FlightSearchCriteria.class), eq(FlightExportService.EXPORT_SORT)))
                .thenReturn(page);
        when(templateEngine.process(eq("flight-log-print"), any(IContext.class)))
                .thenReturn("<html><body><h1>Flugbuch</h1></body></html>");

        byte[] pdf = service.generateExportPdf(new FlightSearchCriteria(), "Flugbuch - Gesamtauszug", "Alle Einträge");

        assertThat(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF");
    }

    @Test
    void generateExportPdf_passesGivenTitleAndSubtitleToTheTemplate() throws IOException {
        initService();
        when(flightService.findAll(any(FlightSearchCriteria.class), eq(FlightExportService.EXPORT_SORT)))
                .thenReturn(Page.empty());
        when(templateEngine.process(eq("flight-log-print"), any(IContext.class)))
                .thenReturn("<html><body>irrelevant for this test</body></html>");

        service.generateExportPdf(new FlightSearchCriteria(), "Flugbuch - Jahresauszug 2026",
                "Zeitraum: 01.01.2026 - 31.12.2026");

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        org.mockito.Mockito.verify(templateEngine).process(eq("flight-log-print"), contextCaptor.capture());
        Context context = contextCaptor.getValue();

        assertThat(context.getVariable("title")).isEqualTo("Flugbuch - Jahresauszug 2026");
        assertThat(context.getVariable("subtitle")).isEqualTo("Zeitraum: 01.01.2026 - 31.12.2026");
    }
}
