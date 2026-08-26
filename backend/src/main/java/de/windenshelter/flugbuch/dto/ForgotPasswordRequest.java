package de.windenshelter.flugbuch.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Body for {@code POST /api/v1/auth/forgot-password}. */
@Data
public class ForgotPasswordRequest {

    @NotBlank
    private String email;
}
