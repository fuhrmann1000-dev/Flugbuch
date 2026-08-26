package de.windenshelter.flugbuch.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.model.Role;
import de.windenshelter.flugbuch.repository.PilotRepository;

/** Plain Mockito unit tests for {@link CustomUserDetailsService} - no Spring context or database required. */
class CustomUserDetailsServiceTest {

    private final PilotRepository pilotRepository = mock(PilotRepository.class);
    private final CustomUserDetailsService userDetailsService = new CustomUserDetailsService(pilotRepository);

    // A pilot's roles must be translated into Spring Security's "ROLE_" authority prefix.
    @Test
    void loadUserByUsername_existingPilot_returnsUserDetailsWithRoles() {
        Pilot pilot = new Pilot();
        pilot.setUsername("Max Mustermann");
        pilot.setEmail("max.mustermann@edpu.de");
        pilot.setPassword("hashed-password");
        pilot.setRoles(Set.of(Role.builder().name("USER").build()));
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(pilot));

        UserDetails userDetails = userDetailsService.loadUserByUsername("max.mustermann@edpu.de");

        assertThat(userDetails.getUsername()).isEqualTo("max.mustermann@edpu.de");
        assertThat(userDetails.getPassword()).isEqualTo("hashed-password");
        assertThat(userDetails.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }

    // The returned object must carry tokenVersion too - JwtAuthenticationFilter
    // relies on this to check a token's validity without a second database query.
    @Test
    void loadUserByUsername_existingPilot_returnsPilotUserDetailsWithTokenVersion() {
        Pilot pilot = new Pilot();
        pilot.setUsername("Max Mustermann");
        pilot.setEmail("max.mustermann@edpu.de");
        pilot.setPassword("hashed-password");
        pilot.setRoles(Set.of(Role.builder().name("USER").build()));
        pilot.setTokenVersion(4);
        when(pilotRepository.findByEmail("max.mustermann@edpu.de")).thenReturn(Optional.of(pilot));

        UserDetails userDetails = userDetailsService.loadUserByUsername("max.mustermann@edpu.de");

        assertThat(userDetails).isInstanceOf(PilotUserDetails.class);
        assertThat(((PilotUserDetails) userDetails).getTokenVersion()).isEqualTo(4);
    }

    // An unknown email must fail loudly instead of silently returning null.
    @Test
    void loadUserByUsername_unknownEmail_throwsUsernameNotFoundException() {
        when(pilotRepository.findByEmail("ghost@edpu.de")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost@edpu.de"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
