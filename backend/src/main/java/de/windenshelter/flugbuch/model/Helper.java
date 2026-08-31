package de.windenshelter.flugbuch.model;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A confirmed volunteer helper for one of the club's "Flatlands" competitions
 * (ticket #54). Rows only ever land here through {@code HelperService#confirm},
 * which applies a {@link HelperConfirmationToken} once its email link has
 * been clicked - there is no direct, unconfirmed write path into this table,
 * so every row here is a real, mail-verified helper.
 */
@Entity
@Table(name = "helpers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Helper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    /**
     * The helper's identity for the create-vs-update decision: submitting
     * the registration form again with an already-known email updates this
     * row instead of creating a second one. See {@code HelperService}.
     */
    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionType competition;

    /**
     * Free-text list of skills (e.g. "winch driver, retrieval, radio"), not
     * a fixed set - the ticket's examples are illustrative, not exhaustive.
     */
    private String skills;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "helper_available_days", joinColumns = @JoinColumn(name = "helper_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private Set<DayOfWeek> availableDays = new HashSet<>();
}
