package de.windenshelter.flugbuch.dto;

import java.time.DayOfWeek;
import java.util.Set;

import de.windenshelter.flugbuch.model.CompetitionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Full helper record, including contact details - only ever served to ADMIN pilots. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelperAdminDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private CompetitionType competition;
    private String skills;
    private Set<DayOfWeek> availableDays;
}
