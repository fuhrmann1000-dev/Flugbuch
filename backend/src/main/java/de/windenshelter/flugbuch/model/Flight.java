package de.windenshelter.flugbuch.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Pattern;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The final, long-term flight record - the union of fields captured across
 * the staging import sources, once those have been merged.
 *
 * Which fields are mandatory depends on {@link #aircraftCategory}: the
 * general logbook rules (registration, landing count) only apply to
 * {@link AircraftCategory#REGISTERED_AIRCRAFT}. Winch-launched free flight
 * ({@link AircraftCategory#FREE_FLIGHT_HG_GS}) follows the lighter DHV
 * rules instead (aircraft type and launch type required, no registration or
 * landing count). Plain field annotations can't express "required only if
 * X", so those per-category rules live in the {@code @AssertTrue} methods
 * below instead of a separate validator class.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {

    // Loose check for "country prefix + registration mark" (e.g. D-MVBO,
    // OE-ABCD, HB-XYZ12) - not a lookup against the real ICAO prefix list.
    private static final Pattern REGISTRATION_PATTERN = Pattern.compile("^[A-Z]{1,2}-[A-Z0-9]{2,7}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AircraftCategory aircraftCategory;

    @NotNull
    @PastOrPresent
    private LocalDate date;

    private String registration;

    @NotBlank
    private String pilot;

    private String aircraftType;

    private String flightType;

    private String launchType;

    @NotBlank
    private String departureAirfield;

    @NotBlank
    private String destinationAirfield;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime landingTime;

    private Integer landingCount;

    private String remarks;

    private Integer guests;
    private String flightDirector;
    private String towedAircraft;
    private Integer towHeight;
    private Double amount;

    @AssertTrue(message = "Registration is required for REGISTERED_AIRCRAFT flights")
    public boolean isRegistrationPresentWhenRequired() {
        if (aircraftCategory != AircraftCategory.REGISTERED_AIRCRAFT) {
            return true;
        }
        return registration != null && !registration.isBlank();
    }

    @AssertTrue(message = "Registration must match <country prefix>-<registration mark> (e.g. D-MVBO)")
    public boolean isRegistrationFormatValid() {
        if (registration == null || registration.isBlank()) {
            return true;
        }
        return REGISTRATION_PATTERN.matcher(registration).matches();
    }

    @AssertTrue(message = "Aircraft type is required for FREE_FLIGHT_HG_GS flights")
    public boolean isAircraftTypePresentWhenRequired() {
        if (aircraftCategory != AircraftCategory.FREE_FLIGHT_HG_GS) {
            return true;
        }
        return aircraftType != null && !aircraftType.isBlank();
    }

    @AssertTrue(message = "Flight type is required for REGISTERED_AIRCRAFT flights")
    public boolean isFlightTypePresentWhenRequired() {
        if (aircraftCategory != AircraftCategory.REGISTERED_AIRCRAFT) {
            return true;
        }
        return flightType != null && !flightType.isBlank();
    }

    @AssertTrue(message = "Launch type is required for FREE_FLIGHT_HG_GS flights")
    public boolean isLaunchTypePresentWhenRequired() {
        if (aircraftCategory != AircraftCategory.FREE_FLIGHT_HG_GS) {
            return true;
        }
        return launchType != null && !launchType.isBlank();
    }

    @AssertTrue(message = "Landing count is required (minimum 1) for REGISTERED_AIRCRAFT flights")
    public boolean isLandingCountValid() {
        if (aircraftCategory == AircraftCategory.REGISTERED_AIRCRAFT) {
            return landingCount != null && landingCount >= 1;
        }
        return landingCount == null || landingCount >= 1;
    }

    @AssertTrue(message = "Landing time cannot be before start time")
    public boolean isLandingTimeNotBeforeStartTime() {
        if (startTime == null || landingTime == null) {
            return true;
        }
        return !landingTime.isBefore(startTime);
    }
}
