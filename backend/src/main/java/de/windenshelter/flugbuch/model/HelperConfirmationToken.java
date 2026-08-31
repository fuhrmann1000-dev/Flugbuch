package de.windenshelter.flugbuch.model;

import java.time.DayOfWeek;
import java.time.Instant;
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
 * A single-use, expiring token holding the data a visitor submitted through
 * the public helper registration form, until they click the confirmation
 * link sent to their email. Nothing in {@link Helper} is created or updated
 * until then - this is what stops anyone from registering a helper (or
 * silently editing someone else's entry) using an email address they don't
 * actually control. See {@code HelperService#submitRegistration} and
 * {@code HelperService#confirm}.
 */
@Entity
@Table(name = "helper_confirmation_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelperConfirmationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    // Proposed values, applied to the matching Helper row (by email) only on confirm.
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionType competition;

    private String skills;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "helper_confirmation_token_days", joinColumns = @JoinColumn(name = "token_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private Set<DayOfWeek> availableDays = new HashSet<>();
}
