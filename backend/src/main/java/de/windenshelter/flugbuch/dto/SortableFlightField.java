package de.windenshelter.flugbuch.dto;

/**
 * The fields a client is allowed to sort {@code GET /api/v1/flights} by.
 * Rendered by Swagger UI as a dropdown (OpenAPI enum) instead of a free-text
 * field, so callers can't typo a sort field. Values use the same public,
 * English names as {@link FlightLogEntryDto}; translating them to the
 * underlying (German) entity field names happens separately, see
 * {@code FlightSortMapping}.
 */
public enum SortableFlightField {

    DATE("date"),
    START_TIME("startTime"),
    LANDING_TIME("landingTime"),
    AIRCRAFT_TYPE("aircraftType"),
    REGISTRATION("registration"),
    PILOT("pilot"),
    GUESTS("guests"),
    FLIGHT_TYPE("flightType"),
    DEPARTURE_AIRFIELD("departureAirfield"),
    DESTINATION_AIRFIELD("destinationAirfield"),
    FLIGHT_DIRECTOR("flightDirector"),
    TOWED_AIRCRAFT("towedAircraft"),
    TOW_HEIGHT("towHeight"),
    AMOUNT("amount"),
    REMARKS("remarks"),
    FLIGHT_COUNT("flightCount");

    private final String dtoFieldName;

    SortableFlightField(String dtoFieldName) {
        this.dtoFieldName = dtoFieldName;
    }

    /** The {@link FlightLogEntryDto} field name to sort by, e.g. {@code "date"}. */
    public String getDtoFieldName() {
        return dtoFieldName;
    }
}
