package de.windenshelter.flugbuch.repository.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import de.windenshelter.flugbuch.dto.FlightSearchCriteria;
import de.windenshelter.flugbuch.dto.FlightType;
import de.windenshelter.flugbuch.model.StagingMainFlightLog;

/**
 * Builds a dynamic, combinable {@link Specification} for
 * {@link StagingMainFlightLog} out of a {@link FlightSearchCriteria}. Only
 * the criteria fields that are actually set contribute a predicate; the rest
 * are simply left out, so any subset of fields (or none at all) can be
 * queried through the same repository method.
 *
 * The free-text filters (pilot, aircraftType, registration) use a
 * case-insensitive partial match (SQL {@code LIKE %value%}), e.g.
 * {@code pilot=Max} matches "Max Mustermann". {@code flightType} is a fixed
 * dropdown (see {@link de.windenshelter.flugbuch.dto.FlightType}), so it's
 * matched exactly instead.
 */
public final class FlightSpecifications {

    private static final char LIKE_ESCAPE_CHAR = '\\';

    private FlightSpecifications() {
    }

    /**
     * Combines every non-empty field on {@code criteria} into a single
     * Specification with AND, e.g. pilot + date both set means "this pilot
     * AND this date". A {@code null} criteria (or one with everything
     * empty) results in a Specification that matches everything.
     */
    public static Specification<StagingMainFlightLog> fromCriteria(FlightSearchCriteria criteria) {
        List<Specification<StagingMainFlightLog>> specs = new ArrayList<>();

        if (criteria == null) {
            return Specification.allOf(specs);
        }

        if (StringUtils.hasText(criteria.getPilot())) {
            specs.add(hasPilot(criteria.getPilot()));
        }
        if (StringUtils.hasText(criteria.getAircraftType())) {
            specs.add(hasAircraftType(criteria.getAircraftType()));
        }
        if (StringUtils.hasText(criteria.getRegistration())) {
            specs.add(hasRegistration(criteria.getRegistration()));
        }
        if (criteria.getFlightType() != null) {
            specs.add(hasFlightType(criteria.getFlightType()));
        }

        if (criteria.getDate() != null) {
            // An exact date takes precedence over a from/to range.
            specs.add(onDate(criteria.getDate()));
        } else if (criteria.getDateFrom() != null || criteria.getDateTo() != null) {
            specs.add(dateBetween(criteria.getDateFrom(), criteria.getDateTo()));
        }

        return Specification.allOf(specs);
    }

    /** Matches entries whose {@code pilot} (entity field) contains {@code pilot}, ignoring case. */
    public static Specification<StagingMainFlightLog> hasPilot(String pilot) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("pilot")), containsPattern(pilot), LIKE_ESCAPE_CHAR);
    }

    /** Matches entries whose {@code muster} (aircraft type) contains {@code aircraftType}, ignoring case. */
    public static Specification<StagingMainFlightLog> hasAircraftType(String aircraftType) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("muster")), containsPattern(aircraftType), LIKE_ESCAPE_CHAR);
    }

    /** Matches entries whose {@code kennzeichen} (registration) contains {@code registration}, ignoring case. */
    public static Specification<StagingMainFlightLog> hasRegistration(String registration) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("kennzeichen")), containsPattern(registration), LIKE_ESCAPE_CHAR);
    }

    /** Matches entries whose {@code flugart} (flight type) equals exactly the selected {@code flightType}. */
    public static Specification<StagingMainFlightLog> hasFlightType(FlightType flightType) {
        return (root, query, cb) -> cb.equal(root.get("flugart"), flightType.getFlugartValue());
    }

    /**
     * Builds a case-insensitive "contains" LIKE pattern, escaping the SQL
     * wildcard characters ({@code %} and {@code _}) that might appear in
     * user input so they're matched literally instead of acting as
     * wildcards.
     */
    private static String containsPattern(String value) {
        String escaped = value.toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    /** Matches entries whose {@code datum} (date) equals exactly {@code date}. */
    public static Specification<StagingMainFlightLog> onDate(LocalDate date) {
        return (root, query, cb) -> cb.equal(root.get("datum"), date);
    }

    /**
     * Inclusive date range. Either bound may be {@code null} to leave that
     * side of the range open.
     */
    public static Specification<StagingMainFlightLog> dateBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.between(root.get("datum"), from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("datum"), from);
            } else {
                return cb.lessThanOrEqualTo(root.get("datum"), to);
            }
        };
    }
}
