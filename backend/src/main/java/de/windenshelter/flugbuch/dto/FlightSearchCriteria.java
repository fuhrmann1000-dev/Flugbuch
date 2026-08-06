package de.windenshelter.flugbuch.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Optional filter criteria for {@code GET /api/v1/flights}. Every field is
 * nullable; a null field means "don't filter on this". Any combination of
 * fields may be supplied at once (e.g. pilot + date, or just dateFrom).
 *
 * If both {@link #date} and a {@link #dateFrom}/{@link #dateTo} range are
 * given, {@link #date} takes precedence for the date restriction.
 *
 * Bound from query params via {@code @ModelAttribute}, so the date fields
 * need {@code @DateTimeFormat} (Spring's data binder, not Jackson) to accept
 * the same dd.MM.yyyy format used elsewhere in the API.
 */
@Data
public class FlightSearchCriteria {

    private String pilot;

    private String aircraftType;

    private String registration;

    private FlightType flightType;

    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonFormat(pattern = "dd.MM.yyyy")
    @Schema(type = "string", pattern = "^\\d{2}\\.\\d{2}\\.\\d{4}$", example = "26.07.2026",
            description = "Exact day to filter on, e.g. today's flights (format dd.MM.yyyy)")
    private LocalDate date;

    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonFormat(pattern = "dd.MM.yyyy")
    @Schema(type = "string", pattern = "^\\d{2}\\.\\d{2}\\.\\d{4}$", example = "01.07.2026",
            description = "Lower bound (inclusive) of a date range (format dd.MM.yyyy)")
    private LocalDate dateFrom;

    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonFormat(pattern = "dd.MM.yyyy")
    @Schema(type = "string", pattern = "^\\d{2}\\.\\d{2}\\.\\d{4}$", example = "31.07.2026",
            description = "Upper bound (inclusive) of a date range (format dd.MM.yyyy)")
    private LocalDate dateTo;
}
