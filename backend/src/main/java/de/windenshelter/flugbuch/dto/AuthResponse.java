package de.windenshelter.flugbuch.dto;

/** The Bearer token to send back on {@code Authorization} headers for subsequent requests. */
public record AuthResponse(String token) {
}
