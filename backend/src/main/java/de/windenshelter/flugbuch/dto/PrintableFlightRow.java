package de.windenshelter.flugbuch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * One table row on the daily flight log printout (see
 * {@code flight-log-print.html}). Every field is already a display-ready
 * String - all formatting (time ranges, null-safe placeholders, etc.)
 * happens once in {@code DailyFlightLogPrintService} instead of inside the
 * Thymeleaf template.
 *
 * A plain class with Lombok-generated getters is used instead of a record:
 * Thymeleaf resolves {@code ${row.timeRange}} through standard JavaBean
 * getter conventions, which a record's accessor-without-"get" methods are
 * not guaranteed to satisfy.
 */
@Getter
@AllArgsConstructor
public class PrintableFlightRow {

    private final String timeRange;
    private final String aircraftType;
    private final String registration;
    private final String pilot;
    private final String guests;
    private final String flightType;
    private final String route;
    private final String flightDirector;
    private final String towedAircraft;
    private final String towHeight;
    private final String amount;
    private final String remarks;
    private final String flightCount;
}
