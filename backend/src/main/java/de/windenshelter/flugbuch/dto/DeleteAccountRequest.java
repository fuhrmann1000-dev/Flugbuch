package de.windenshelter.flugbuch.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Body for {@code DELETE /api/v1/pilots/me}: the pilot must re-enter their password to confirm. */
@Data
public class DeleteAccountRequest {

    @NotBlank
    private String password;
}
