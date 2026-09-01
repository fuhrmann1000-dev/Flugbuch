package de.windenshelter.flugbuch.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import de.windenshelter.flugbuch.configuration.print.PrintProperties;
import de.windenshelter.flugbuch.dto.FlightLogEntryDto;
import de.windenshelter.flugbuch.dto.FlightSearchCriteria;
import de.windenshelter.flugbuch.dto.PrintableFlightRow;
import lombok.RequiredArgsConstructor;

/**
 * Renders a flight log excerpt as a PDF using the {@code flight-log-print.html}
 * Thymeleaf template. {@link #generateDailyPrint} is the original use (one
 * day, written to the print output directory as
 * {@code flugbuch-<yyyy-MM-dd>.pdf}, ticket #daily-print); {@link
 * #generateExportPdf} is the newer on-demand "Export Data" use (an
 * arbitrary date range, returned as bytes for a controller to stream back
 * to the browser instead of writing to disk).
 *
 * A day with zero flights still produces a PDF - just with the template's
 * empty-state message instead of a table - so there's always exactly one
 * file per day, which keeps the scheduler and any later archival tooling
 * simple (no "did today even generate anything?" branch to handle).
 */
@Service
@RequiredArgsConstructor
public class DailyFlightLogPrintService {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_TIMESTAMP = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm");

    private final FlightService flightService;
    private final TemplateEngine templateEngine;
    private final PrintProperties printProperties;

    /** Generates the PDF for {@code date} and returns the path it was written to. */
    public Path generateDailyPrint(LocalDate date) throws IOException {
        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setDate(date);
        Page<FlightLogEntryDto> flights = flightService.findAll(criteria, Pageable.unpaged());

        String html = renderHtml("Flugbuch - Tagesauszug", date.format(DISPLAY_DATE), flights.getContent());

        Path outputDirectory = Path.of(printProperties.getOutputDirectory());
        Files.createDirectories(outputDirectory);
        Path outputFile = outputDirectory.resolve("flugbuch-" + date.format(FILE_DATE) + ".pdf");

        try (OutputStream out = Files.newOutputStream(outputFile)) {
            renderPdf(html, out);
        }

        return outputFile;
    }

    /**
     * Generates a PDF for an arbitrary date range (see {@code
     * FlightExportService#resolveRange}) and returns it as bytes, for the
     * "Export Data" card on the Data Management page - no file is written
     * to disk, unlike {@link #generateDailyPrint}.
     */
    public byte[] generateExportPdf(FlightSearchCriteria criteria, String title, String subtitle) throws IOException {
        Page<FlightLogEntryDto> flights = flightService.findAll(criteria, FlightExportService.EXPORT_SORT);
        String html = renderHtml(title, subtitle, flights.getContent());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            renderPdf(html, out);
            return out.toByteArray();
        }
    }

    private void renderPdf(String html, OutputStream out) throws IOException {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        // No external resources (images, CSS files) are referenced by the
        // template, so there's nothing for a base URI to resolve - an
        // empty string is the safe choice OpenHTMLtoPDF expects here.
        builder.withHtmlContent(html, "");
        builder.toStream(out);
        builder.run();
    }

    private String renderHtml(String title, String subtitle, List<FlightLogEntryDto> flights) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("subtitle", subtitle);
        context.setVariable("rows", flights.stream().map(this::toRow).toList());
        context.setVariable("generatedAt", "Erstellt am " + LocalDateTime.now().format(DISPLAY_TIMESTAMP));
        return templateEngine.process("flight-log-print", context);
    }

    private PrintableFlightRow toRow(FlightLogEntryDto dto) {
        return new PrintableFlightRow(
                formatTimeRange(dto.getStartTime(), dto.getLandingTime()),
                nullSafe(dto.getAircraftType()),
                nullSafe(dto.getRegistration()),
                nullSafe(dto.getPilot()),
                dto.getGuests() == null ? "-" : String.valueOf(dto.getGuests()),
                nullSafe(dto.getFlightType()),
                formatRoute(dto.getDepartureAirfield(), dto.getDestinationAirfield()),
                nullSafe(dto.getFlightDirector()),
                nullSafe(dto.getTowedAircraft()),
                dto.getTowHeight() == null ? "-" : String.valueOf(dto.getTowHeight()),
                dto.getAmount() == null ? "-" : String.format("%.2f", dto.getAmount()),
                nullSafe(dto.getRemarks()),
                dto.getFlightCount() == null ? "-" : String.valueOf(dto.getFlightCount())
        );
    }

    private String formatTimeRange(LocalTime startTime, LocalTime landingTime) {
        // Plain ASCII "-" rather than an en dash / arrow glyph: OpenHTMLtoPDF's
        // default (non-embedded) base fonts don't have glyphs for those, so
        // using them here risks missing/garbled characters in the PDF.
        String start = startTime == null ? "-" : startTime.format(DISPLAY_TIME);
        String landing = landingTime == null ? "-" : landingTime.format(DISPLAY_TIME);
        return start + " - " + landing;
    }

    private String formatRoute(String departureAirfield, String destinationAirfield) {
        return nullSafe(departureAirfield) + " -> " + nullSafe(destinationAirfield);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
