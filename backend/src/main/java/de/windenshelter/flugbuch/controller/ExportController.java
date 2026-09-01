package de.windenshelter.flugbuch.controller;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.windenshelter.flugbuch.dto.ExportRange;
import de.windenshelter.flugbuch.service.DailyFlightLogPrintService;
import de.windenshelter.flugbuch.service.FlightExportService;
import de.windenshelter.flugbuch.service.FlightExportService.ExportRangeInfo;
import lombok.RequiredArgsConstructor;

/**
 * Backs the Data Management page's "Export Data" card: any logged-in pilot
 * (see {@code SecurityConfig}'s default {@code anyRequest().authenticated()}
 * - no extra rule needed here) can download the flight log as CSV or PDF,
 * narrowed to all entries / the current year / the current month.
 */
@RestController
@RequestMapping("/api/v1/exports")
@RequiredArgsConstructor
public class ExportController {

    private final FlightExportService flightExportService;
    private final DailyFlightLogPrintService dailyFlightLogPrintService;

    /** Same column layout the manual CSV import expects - see {@link FlightExportService}. */
    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(defaultValue = "ALL") ExportRange range) {
        ExportRangeInfo info = flightExportService.resolveRange(range);
        byte[] csv = flightExportService.toCsv(info.criteria());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment("flugbuch-export.csv"))
                .body(csv);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(defaultValue = "ALL") ExportRange range) {
        ExportRangeInfo info = flightExportService.resolveRange(range);
        try {
            byte[] pdf = dailyFlightLogPrintService.generateExportPdf(info.criteria(), info.title(), info.subtitle());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, attachment("flugbuch-export.pdf"))
                    .body(pdf);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate the PDF export");
        }
    }

    private String attachment(String baseFilename) {
        String dated = baseFilename.replaceFirst("\\.", "-" + LocalDate.now() + ".");
        return ContentDisposition.attachment().filename(dated).build().toString();
    }
}
