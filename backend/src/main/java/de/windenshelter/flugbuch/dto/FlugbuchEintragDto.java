package de.windenshelter.flugbuch.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FlugbuchEintragDto {
    private Long id;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    @Schema(type = "string", pattern = "dd.MM.yyyy HH:mm", example = "26.07.2026 19:05")
    private LocalDateTime flugDatum;
    private String kundenNummer;
    private String nameDesPiloten;
}