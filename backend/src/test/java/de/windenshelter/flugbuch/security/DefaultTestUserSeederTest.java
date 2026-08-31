package de.windenshelter.flugbuch.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.model.Role;
import de.windenshelter.flugbuch.repository.PilotRepository;
import de.windenshelter.flugbuch.repository.RoleRepository;

/** Plain Mockito unit tests for {@link DefaultTestUserSeeder} - no Spring context or database required. */
class DefaultTestUserSeederTest {

    private final PilotRepository pilotRepository = mock(PilotRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final DefaultTestUserSeeder seeder =
            new DefaultTestUserSeeder(pilotRepository, roleRepository, passwordEncoder);

    // First startup: no test pilot yet, so one gets created with the USER role (not ADMIN) and a hashed password.
    @Test
    void run_noTestUserYet_createsOneWithUserRoleAndHashedPassword() {
        when(pilotRepository.existsByEmail("pilot@flugbuch.local")).thenReturn(false);
        Role userRole = Role.builder().id(1L).name("USER").build();
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("TestPilot123")).thenReturn("hashed-password");

        seeder.run();

        ArgumentCaptor<Pilot> pilotCaptor = ArgumentCaptor.forClass(Pilot.class);
        verify(pilotRepository).save(pilotCaptor.capture());
        Pilot savedPilot = pilotCaptor.getValue();

        assertThat(savedPilot.getPassword()).isEqualTo("hashed-password");
        assertThat(savedPilot.getRoles()).containsExactly(userRole);
        assertThat(savedPilot.getEmail()).isEqualTo("pilot@flugbuch.local");

        // Placeholder profile data must be populated too, so GET /pilots/me
        // has something real to return for this test account right away.
        assertThat(savedPilot.getFirstName()).isNotBlank();
        assertThat(savedPilot.getLastName()).isNotBlank();
        assertThat(savedPilot.getPhone()).isNotBlank();
        assertThat(savedPilot.getLicenseType()).isNotBlank();
        assertThat(savedPilot.getLicenseNumber()).isNotBlank();
        assertThat(savedPilot.getHomeAirfield()).isNotBlank();
    }

    // Every later startup: the test pilot already exists, so nothing should be created again.
    @Test
    void run_testUserAlreadyExists_doesNothing() {
        when(pilotRepository.existsByEmail("pilot@flugbuch.local")).thenReturn(true);

        seeder.run();

        verify(pilotRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // Defensive check: if DefaultRoleSeeder somehow hasn't run yet, fail loudly instead of silently skipping.
    @Test
    void run_userRoleMissing_throwsIllegalStateException() {
        when(pilotRepository.existsByEmail("pilot@flugbuch.local")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seeder.run())
                .isInstanceOf(IllegalStateException.class);
    }
}
