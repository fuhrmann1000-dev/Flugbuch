package de.windenshelter.flugbuch.repository.specification;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import de.windenshelter.flugbuch.dto.FlightLogEntryDto;
import de.windenshelter.flugbuch.model.StagingMainFlightLog;

/**
 * Translates {@code sort} properties from the API's public
 * {@link FlightLogEntryDto} field names (English, e.g. {@code date},
 * {@code aircraftType}) to the underlying {@link StagingMainFlightLog}
 * entity's field names (German, e.g. {@code datum}, {@code muster}), so a
 * client can request {@code ?sort=date,desc} without needing to know the
 * internal entity property names. Any property not in the map is passed
 * through unchanged.
 */
public final class FlightSortMapping {

    private static final Map<String, String> DTO_TO_ENTITY_FIELD = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("date", "datum"),
            Map.entry("startTime", "startzeit"),
            Map.entry("landingTime", "landezeit"),
            Map.entry("aircraftType", "muster"),
            Map.entry("registration", "kennzeichen"),
            Map.entry("pilot", "pilot"),
            Map.entry("guests", "gaeste"),
            Map.entry("flightType", "flugart"),
            Map.entry("departureAirfield", "startPlatz"),
            Map.entry("destinationAirfield", "zielPlatz"),
            Map.entry("flightDirector", "flugLeiter"),
            Map.entry("towedAircraft", "geschleppter"),
            Map.entry("towHeight", "schleppHoehe"),
            Map.entry("amount", "betrag"),
            Map.entry("remarks", "bemerkung"),
            Map.entry("flightCount", "flugAnzahl")
    );

    private FlightSortMapping() {
    }

    public static Pageable toEntitySort(Pageable pageable) {
        if (pageable.isUnpaged() || pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> translatedOrders = pageable.getSort().stream()
                .map(FlightSortMapping::translateOrder)
                .toList();

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(translatedOrders));
    }

    private static Sort.Order translateOrder(Sort.Order order) {
        String entityProperty = DTO_TO_ENTITY_FIELD.getOrDefault(order.getProperty(), order.getProperty());
        Sort.Order translated = new Sort.Order(order.getDirection(), entityProperty, order.getNullHandling());
        return order.isIgnoreCase() ? translated.ignoreCase() : translated;
    }
}
