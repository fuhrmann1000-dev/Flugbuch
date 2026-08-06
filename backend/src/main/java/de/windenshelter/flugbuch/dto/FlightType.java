package de.windenshelter.flugbuch.dto;

/**
 * The kinds of flight a caller can filter {@code GET /api/v1/flights} by.
 * Rendered by Swagger UI as a dropdown (OpenAPI enum) instead of a free-text
 * field. These are placeholder values (TYPE_1, TYPE_2, ...) - rename/extend
 * them once the real list of flight types is known.
 */
public enum FlightType {

    TYPE_1("Type 1"),
    TYPE_2("Type 2");

    private final String flugartValue;

    FlightType(String flugartValue) {
        this.flugartValue = flugartValue;
    }

    /** The exact {@code flugart} value stored on {@code StagingMainFlightLog} entries of this type. */
    public String getFlugartValue() {
        return flugartValue;
    }
}
