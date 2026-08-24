package de.windenshelter.flugbuch.model;

/**
 * Which regulatory profile a {@link Flight} record must satisfy: full
 * logbook rules for registered aircraft, or the lighter DHV rules for
 * winch-launched free flight (hang glider/paraglider), which aren't
 * registered aircraft in the first place.
 */
public enum AircraftCategory {
    REGISTERED_AIRCRAFT,
    FREE_FLIGHT_HG_GS
}
