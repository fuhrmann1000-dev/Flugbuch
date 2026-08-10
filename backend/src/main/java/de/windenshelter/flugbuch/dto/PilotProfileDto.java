package de.windenshelter.flugbuch.dto;

import lombok.Data;

/** What GET/PUT {@code /api/v1/pilots/me} returns: the authenticated pilot's profile data. */
@Data
public class PilotProfileDto {

    private Long id;

    /** Login identity - read-only here, never changed through the profile endpoints. */
    private String username;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String licenseType;
    private String licenseNumber;
    private String homeAirfield;

    /** Base64 data URI, or null if the pilot hasn't uploaded one - see {@link de.windenshelter.flugbuch.model.Pilot#getProfilePicture()}. */
    private String profilePicture;
}
