package de.windenshelter.flugbuch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Body for {@code PUT /api/v1/pilots/me}. Username and password are not editable here. */
@Data
public class UpdatePilotProfileRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    /** The pilot's login identity - required and must stay unique; see {@code PilotService#updateMyProfile}. */
    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 50)
    private String phone;

    @Size(max = 50)
    private String licenseType;

    @Size(max = 50)
    private String licenseNumber;

    @Size(max = 150)
    private String homeAirfield;
}
