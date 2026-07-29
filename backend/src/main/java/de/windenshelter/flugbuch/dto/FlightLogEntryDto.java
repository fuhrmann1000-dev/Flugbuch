package de.windenshelter.flugbuch.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@JsonPropertyOrder({"id"})
public class FlightLogEntryDto {
    private Long id;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @Schema(type = "string", pattern = "^\\d{2}\\.\\d{2}\\.\\d{4}$", example = "26.07.2026")
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", pattern = "^\\d{2}:\\d{2}$", example = "19:05")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", pattern = "^\\d{2}:\\d{2}$", example = "19:35")
    private LocalTime landingTime;

    private String aircraftType;
    private String registration;
    private String pilot;
    private Integer guests;
    private String flightType;
    private String departureAirfield;
    private String destinationAirfield;
    private String flightDirector;
    private String towedAircraft;
    private Integer towHeight;
    private Double amount;
    private String remarks;
    private Integer flightCount;
}