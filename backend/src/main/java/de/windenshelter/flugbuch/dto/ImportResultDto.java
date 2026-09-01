package de.windenshelter.flugbuch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Outcome of a manual CSV import: how many rows were stored vs. already known (skipped as duplicates). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDto {

    private int imported;
    private int skipped;
}
