package de.windenshelter.flugbuch.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A registered pilot account: login credentials plus the roles that grant API access. */
@Entity
@Table(name = "pilots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pilot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Profile fields (ticket: functional profile page). All nullable - a pilot
    // created via /auth/register only has username/password/roles until they
    // fill these in on the Profile page themselves.
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String licenseType;
    private String licenseNumber;
    private String homeAirfield;

    /**
     * The avatar image as a base64 data URI (e.g. {@code data:image/png;base64,...}),
     * so the frontend can drop it straight into an {@code <img src>} with no
     * separate file-serving endpoint needed. Plain {@code TEXT} instead of
     * {@code @Lob} - simpler and more predictable across H2 (dev/tests) and
     * Postgres (prod) than JDBC CLOB streaming, and this is read/written
     * eagerly as a whole string anyway. Null means "no picture uploaded yet -
     * show the initials avatar instead" (frontend concern, not enforced here).
     */
    @Column(columnDefinition = "TEXT")
    private String profilePicture;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "pilot_roles",
            joinColumns = @JoinColumn(name = "pilot_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}
