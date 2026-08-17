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
        pilot.setUsername("max.mustermann");
        pilot.setPassword("hashed-password");
        pilot.setRoles(Set.of(Role.builder().name("USER").build()));
        when(pilotRepository.findByUsername("max.mustermann")).thenReturn(Optional.of(pilot));

        UserDetails userDetails = userDetailsService.loadUserByUsername("max.mustermann");

        assertThat(userDetails.getUsername()).isEqualTo("max.mustermann");
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
        pilot.setUsername("max.mustermann");
        pilot.setPassword("hashed-password");
        pilot.setRoles(Set.of(Role.builder().name("USER").build()));
        pilot.setTokenVersion(4);
        when(pilotRepository.findByUsername("max.mustermann")).thenReturn(Optional.of(pilot));

        UserDetails userDetails = userDetailsService.loadUserByUsername("max.mustermann");

        assertThat(userDetails).isInstanceOf(PilotUserDetails.class);
        assertThat(((PilotUserDetails) userDetails).getTokenVersion()).isEqualTo(4);
    }

    // An unknown username must fail loudly instead of silently returning null.
    @Test
    void loadUserByUsername_unknownUsername_throwsUsernameNotFoundException() {
        when(pilotRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
