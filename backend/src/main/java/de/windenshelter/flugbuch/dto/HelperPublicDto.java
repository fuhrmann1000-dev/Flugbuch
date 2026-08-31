package de.windenshelter.flugbuch.dto;

import java.time.DayOfWeek;
import java.util.Set;

import de.windenshelter.flugbuch.model.CompetitionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * What any visitor - helper or not, no login required - may see about a
 * confirmed helper: first name, competition, skills and availability.
 * Deliberately excludes last name, phone and email; see {@link HelperAdminDto}
 * for the full record, which is ADMIN-only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelperPublicDto {

    private String firstName;
    private CompetitionType competition;
    private String skills;
    private Set<DayOfWeek> availableDays;
}
