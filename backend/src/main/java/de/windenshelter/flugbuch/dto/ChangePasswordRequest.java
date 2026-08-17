package de.windenshelter.flugbuch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Body for {@code PUT /api/v1/pilots/me/password}. */
@Data
public class ChangePasswordRequest {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    // At least one lowercase letter, one uppercase letter and one digit
    // (special characters are allowed but not required). Only applies to
    // the *new* password - currentPassword is just checked for equality
    // against what's already stored, however weak that may be.
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter and one digit")
    private String newPassword;
}
