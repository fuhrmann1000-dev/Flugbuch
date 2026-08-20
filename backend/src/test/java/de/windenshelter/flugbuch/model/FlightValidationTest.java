package de.windenshelter.flugbuch.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * Proves the {@link Flight} validation rules: the general logbook rules
 * apply to {@link AircraftCategory#REGISTERED_AIRCRAFT}, while
 * {@link AircraftCategory#FREE_FLIGHT_HG_GS} follows the lighter DHV rules
 * instead. Uses a plain Jakarta Bean Validation {@link Validator} directly
 * on the entity - no Spring context or database needed.
 */
class FlightValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Flight.FlightBuilder validRegisteredAircraft() {
        return Flight.builder()
                .aircraftCategory(AircraftCategory.REGISTERED_AIRCRAFT)
                .date(LocalDate.now())
                .registration("D-MVBO")
                .pilot("Max Mustermann")
                .flightType("Schulung")
                .departureAirfield("EDKA")
                .destinationAirfield("EDKA")
                .startTime(LocalTime.of(9, 0))
                .landingTime(LocalTime.of(9, 30))
                .landingCount(1);
    }

    private Flight.FlightBuilder validFreeFlightHgGs() {
        return Flight.builder()
                .aircraftCategory(AircraftCategory.FREE_FLIGHT_HG_GS)
                .date(LocalDate.now())
                .pilot("Erika Musterfrau")
                .aircraftType("Advance Iota 2")
                .launchType("Windenschlepp")
                .departureAirfield("EDKA")
                .destinationAirfield("EDKA")
                .startTime(LocalTime.of(10, 0))
                .landingTime(LocalTime.of(10, 20));
    }

    private boolean hasViolationMatching(Set<ConstraintViolation<Flight>> violations, String messageFragment) {
        return violations.stream().anyMatch(v -> v.getMessage().contains(messageFragment));
    }

    @Test
    void registeredAircraft_allFieldsPresent_noViolations() {
        assertThat(validator.validate(validRegisteredAircraft().build())).isEmpty();
    }

    @Test
    void freeFlightHgGs_allFieldsPresent_noViolations() {
        assertThat(validator.validate(validFreeFlightHgGs().build())).isEmpty();
    }

    @Test
    void registeredAircraft_missingRegistration_violates() {
        Flight flight = validRegisteredAircraft().registration(null).build();

        assertThat(hasViolationMatching(validator.validate(flight), "Registration is required")).isTrue();
    }

    @Test
    void registeredAircraft_registrationWrongFormat_violates() {
        Flight flight = validRegisteredAircraft().registration("NOTVALID123456").build();

        assertThat(hasViolationMatching(validator.validate(flight), "must match")).isTrue();
    }

    @Test
    void freeFlightHgGs_registrationNotRequired_noViolation() {
        assertThat(validator.validate(validFreeFlightHgGs().registration(null).build())).isEmpty();
    }

    @Test
    void freeFlightHgGs_missingAircraftType_violates() {
        Flight flight = validFreeFlightHgGs().aircraftType(null).build();

        assertThat(hasViolationMatching(validator.validate(flight), "Aircraft type is required")).isTrue();
    }

    @Test
    void freeFlightHgGs_missingLaunchType_violates() {
        Flight flight = validFreeFlightHgGs().launchType(null).build();

        assertThat(hasViolationMatching(validator.validate(flight), "Launch type is required")).isTrue();
    }

    @Test
    void registeredAircraft_aircraftTypeAndLaunchTypeNotRequired_noViolation() {
        Flight flight = validRegisteredAircraft().aircraftType(null).launchType(null).build();

        assertThat(validator.validate(flight)).isEmpty();
    }

    @Test
    void registeredAircraft_missingFlightType_violates() {
        Flight flight = validRegisteredAircraft().flightType(null).build();

        assertThat(hasViolationMatching(validator.validate(flight), "Flight type is required")).isTrue();
    }

    @Test
    void freeFlightHgGs_flightTypeNotRequired_noViolation() {
        assertThat(validator.validate(validFreeFlightHgGs().flightType(null).build())).isEmpty();
    }

    @Test
    void registeredAircraft_missingLandingCount_violates() {
        Flight flight = validRegisteredAircraft().landingCount(null).build();

        assertThat(hasViolationMatching(validator.validate(flight), "Landing count is required")).isTrue();
    }

    @Test
    void registeredAircraft_landingCountZero_violates() {
        Flight flight = validRegisteredAircraft().landingCount(0).build();

        assertThat(hasViolationMatching(validator.validate(flight), "Landing count is required")).isTrue();
    }

    @Test
    void freeFlightHgGs_landingCountOptional_noViolationWhenNull() {
        assertThat(validator.validate(validFreeFlightHgGs().landingCount(null).build())).isEmpty();
    }

    @Test
    void freeFlightHgGs_landingCountZeroIfProvided_stillViolates() {
        Flight flight = validFreeFlightHgGs().landingCount(0).build();

        assertThat(hasViolationMatching(validator.validate(flight), "Landing count is required")).isTrue();
    }

    @Test
    void landingTimeBeforeStartTime_violates() {
        Flight flight = validRegisteredAircraft()
                .startTime(LocalTime.of(10, 0))
                .landingTime(LocalTime.of(9, 59))
                .build();

        assertThat(hasViolationMatching(validator.validate(flight), "Landing time cannot be before")).isTrue();
    }

    @Test
    void dateInTheFuture_violates() {
        Flight flight = validRegisteredAircraft().date(LocalDate.now().plusDays(1)).build();

        assertThat(validator.validate(flight)).isNotEmpty();
    }

    @Test
    void missingPilot_violates() {
        Flight flight = validRegisteredAircraft().pilot(" ").build();

        assertThat(validator.validate(flight)).isNotEmpty();
    }

    @Test
    void missingDepartureAirfield_violates() {
        Flight flight = validRegisteredAircraft().departureAirfield(null).build();

        assertThat(validator.validate(flight)).isNotEmpty();
    }
}
