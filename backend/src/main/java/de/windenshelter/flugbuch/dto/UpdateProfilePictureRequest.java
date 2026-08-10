package de.windenshelter.flugbuch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Body for {@code PUT /api/v1/pilots/me/picture}. */
@Data
public class UpdateProfilePictureRequest {

    @NotBlank
    @Pattern(regexp = "^data:image/(png|jpe?g|gif|webp);base64,.+",
            message = "Must be a base64-encoded image data URI (png, jpg, gif or webp)")
    private String profilePicture;
}
