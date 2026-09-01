package de.windenshelter.flugbuch.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import de.windenshelter.flugbuch.dto.ExportRange;
import de.windenshelter.flugbuch.dto.FlightLogEntryDto;
import de.windenshelter.flugbuch.dto.FlightSearchCriteria;
import lombok.RequiredArgsConstructor;

/**
 * Backs the Data Management page's "Export Data" card: resolves the chosen
 * {@link ExportRange} into an actual date window, and serializes the
 * matching flights as CSV. PDF export is handled separately by
 * {@link DailyFlightLogPrintService#generateExportPdf}, which reuses the
 * same {@link #resolveRange} output.
 *
 * The CSV uses the exact same column layout the manual CSV import expects
 * (see {@code MainFlightLogImportService}) - semicolon-separated, German
 * headers, dd.MM.yyyy/HH:mm - so an exported file can be re-imported
 * unchanged, and a UTF-8 BOM is prepended so Excel opens it with umlauts
 * intact instead of guessing the wrong encoding.
 */
@Service
@RequiredArgsConstructor
public class FlightExportService {

    private static final DateTimeFormatter CSV_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter CSV_TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.GERMAN);
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private static final String CSV_HEADER =
            "Datum;Startzeit;Landezeit;Muster;Kennzeichen;Pilot;Gäste;Flugart;Startplatz;Zielplatz;"
                    + "Flugleiter;Geschleppter;Schlepphöhe;Betrag;Bemerkung;Fluganzahl";

    /** Chronological, so both the CSV and the PDF read top-to-bottom by date. */
    static final Pageable EXPORT_SORT = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("date", "startTime"));

    private final FlightService flightService;

    /** The date window plus a human-readable (German) title/subtitle pair for the PDF's header. */
    public record ExportRangeInfo(FlightSearchCriteria criteria, String title, String subtitle) {
    }

    public ExportRangeInfo resolveRange(ExportRange range) {
        LocalDate today = LocalDate.now();
        FlightSearchCriteria criteria = new FlightSearchCriteria();

        return switch (range) {
            case YEAR -> {
                LocalDate from = today.withDayOfYear(1);
                LocalDate to = today.withDayOfYear(today.lengthOfYear());
                criteria.setDateFrom(from);
                criteria.setDateTo(to);
                yield new ExportRangeInfo(criteria, "Flugbuch - Jahresauszug " + today.getYear(),
                        "Zeitraum: " + from.format(CSV_DATE) + " - " + to.format(CSV_DATE));
            }
            case MONTH -> {
                LocalDate from = today.withDayOfMonth(1);
                LocalDate to = today.withDayOfMonth(today.lengthOfMonth());
                criteria.setDateFrom(from);
                criteria.setDateTo(to);
                yield new ExportRangeInfo(criteria, "Flugbuch - Monatsauszug " + capitalize(today.format(MONTH_LABEL)),
                        "Zeitraum: " + from.format(CSV_DATE) + " - " + to.format(CSV_DATE));
            }
            default -> new ExportRangeInfo(criteria, "Flugbuch - Gesamtauszug", "Alle Einträge");
        };
    }

    /** Fetches the flights for {@code criteria} and serializes them as UTF-8 (with BOM) CSV bytes. */
    public byte[] toCsv(FlightSearchCriteria criteria) {
        List<FlightLogEntryDto> flights = flightService.findAll(criteria, EXPORT_SORT).getContent();

        StringBuilder csv = new StringBuilder(CSV_HEADER).append("\r\n");
        for (FlightLogEntryDto flight : flights) {
            csv.append(row(flight)).append("\r\n");
        }

        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, result, UTF8_BOM.length, body.length);
        return result;
    }

    private String row(FlightLogEntryDto f) {
        return String.join(";",
                field(f.getDate() == null ? null : f.getDate().format(CSV_DATE)),
                field(f.getStartTime() == null ? null : f.getStartTime().format(CSV_TIME)),
                field(f.getLandingTime() == null ? null : f.getLandingTime().format(CSV_TIME)),
                field(f.getAircraftType()),
                field(f.getRegistration()),
                field(f.getPilot()),
                field(f.getGuests() == null ? null : String.valueOf(f.getGuests())),
                field(f.getFlightType()),
                field(f.getDepartureAirfield()),
                field(f.getDestinationAirfield()),
                field(f.getFlightDirector()),
                field(f.getTowedAircraft()),
                field(f.getTowHeight() == null ? null : String.valueOf(f.getTowHeight())),
                field(f.getAmount() == null ? null : String.valueOf(f.getAmount())),
                field(f.getRemarks()),
                field(f.getFlightCount() == null ? null : String.valueOf(f.getFlightCount())));
    }

    /** Quotes a field (doubling any inner quotes) only if it actually needs it - matches CsvLineParser's reading rules. */
    private String field(String value) {
        if (value == null) {
            return "";
        }
        if (value.indexOf(';') < 0 && value.indexOf('"') < 0 && value.indexOf('\n') < 0) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String capitalize(String value) {
        return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
