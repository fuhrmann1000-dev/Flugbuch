package de.windenshelter.flugbuch.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.model.Role;
import de.windenshelter.flugbuch.repository.PilotRepository;
import de.windenshelter.flugbuch.repository.RoleRepository;

/** Plain Mockito unit tests for {@link DefaultAdminSeeder} - no Spring context or database required. */
class DefaultAdminSeederTest {

    private final PilotRepository pilotRepository = mock(PilotRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AdminProperties adminProperties = new AdminProperties();
    private final DefaultAdminSeeder seeder =
            new DefaultAdminSeeder(pilotRepository, roleRepository, passwordEncoder, adminProperties);

    @BeforeEach
    void setUp() {
        adminProperties.setUsername("admin");
        adminProperties.setPassword("plainTextPassword");
    }

    // First startup: no admin yet, so one gets created with the ADMIN role and a hashed password.
    @Test
    void run_noAdminYet_createsOneWithAdminRoleAndHashedPassword() {
        when(pilotRepository.existsByUsername("admin")).thenReturn(false);
        Role adminRole = Role.builder().id(1L).name("ADMIN").build();
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("plainTextPassword")).thenReturn("hashed-password");

        seeder.run();

        ArgumentCaptor<Pilot> pilotCaptor = ArgumentCaptor.forClass(Pilot.class);
        verify(pilotRepository).save(pilotCaptor.capture());
        Pilot savedAdmin = pilotCaptor.getValue();

        assertThat(savedAdmin.getUsername()).isEqualTo("admin");
        assertThat(savedAdmin.getPassword()).isEqualTo("hashed-password");
        assertThat(savedAdmin.getRoles()).containsExactly(adminRole);

        // Placeholder profile data must be populated too, so GET /pilots/me
        // has something real to return for this test account right away.
        assertThat(savedAdmin.getFirstName()).isNotBlank();
        assertThat(savedAdmin.getLastName()).isNotBlank();
        assertThat(savedAdmin.getEmail()).isNotBlank();
        assertThat(savedAdmin.getPhone()).isNotBlank();
        assertThat(savedAdmin.getLicenseType()).isNotBlank();
        assertThat(savedAdmin.getLicenseNumber()).isNotBlank();
        assertThat(savedAdmin.getHomeAirfield()).isNotBlank();
    }

    // Every later startup: an admin already exists, so nothing should be created again.
    @Test
    void run_adminAlreadyExists_doesNothing() {
        when(pilotRepository.existsByUsername("admin")).thenReturn(true);

        seeder.run();

        verify(pilotRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // Defensive check: if DefaultRoleSeeder somehow hasn't run yet, fail loudly instead of silently skipping.
    @Test
    void run_adminRoleMissing_throwsIllegalStateException() {
        when(pilotRepository.existsByUsername("admin")).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seeder.run())
                .isInstanceOf(IllegalStateException.class);
    }
}
