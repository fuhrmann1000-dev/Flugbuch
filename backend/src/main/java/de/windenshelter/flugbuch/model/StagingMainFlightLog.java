package de.windenshelter.flugbuch.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StagingMainFlightLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Integer externalId;

    private LocalDate datum;
    private LocalTime startzeit;
    private LocalTime landezeit;
    private String muster;
    private String kennzeichen;
    private String pilot;
    private Integer gaeste;
    private String flugart;
    private String startPlatz;
    private String zielPlatz;
    private String flugLeiter;
    private String geschleppter;
    private Integer schleppHoehe;
    private Double betrag;
    private String bemerkung;
    private Integer flugAnzahl;


}
