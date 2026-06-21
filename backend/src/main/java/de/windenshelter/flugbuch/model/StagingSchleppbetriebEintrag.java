package de.windenshelter.flugbuch.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StagingSchleppbetriebEintrag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Integer externalId;

    private Integer vereinId;
    private LocalDateTime zeitpunkt;
    private Integer pilotNr;
    private String pilot;
    private String typ;
    private Integer windenfahrerNr;
    private String windenfahrer;
    private Integer startleiterNr;
    private String startleiter;
    private String windeName;
    private String zusatz;
    private String status; // PENDING / VALIDATED / MERGED / QUARANTINED
}
