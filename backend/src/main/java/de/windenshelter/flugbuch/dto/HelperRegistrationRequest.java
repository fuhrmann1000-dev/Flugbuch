package de.windenshelter.flugbuch.dto;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

import de.windenshelter.flugbuch.model.CompetitionType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Body for {@code POST /api/v1/helpers/register} - the single public
 * create-or-update form. Whether this ends up creating a new helper or
 * updating an existing one is decided later, by {@code HelperService}, based
 * on whether {@link #email} is already known - not by anything in this DTO.
 */
@Data
public class HelperRegistrationRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phone;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private CompetitionType competition;

    /** Free-text list of skills, e.g. "winch driver, retrieval, radio". */
    private String skills;

    private Set<DayOfWeek> availableDays = new HashSet<>();
}
