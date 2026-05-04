package de.windenshelter.flugbuch.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FlugbuchEintragDto {
    private LocalDateTime flugDatum;
    private String kundenNummer;
    private String nameDesPiloten;
}