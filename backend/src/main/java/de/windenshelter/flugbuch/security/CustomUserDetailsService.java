package de.windenshelter.flugbuch.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.repository.PilotRepository;
import lombok.RequiredArgsConstructor;

/**
 * Loads a {@link Pilot} from the database and adapts it to what Spring
 * Security expects. The {@code username} parameter required by the
 * {@link UserDetailsService} interface actually carries the pilot's email -
 * email, not username, is this app's unique login identity.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PilotRepository pilotRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        Pilot pilot = pilotRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No pilot found with email " + email));

        List<SimpleGrantedAuthority> authorities = pilot.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();

        // PilotUserDetails (not a plain User) so JwtAuthenticationFilter can
        // read tokenVersion off the object it already asked us to load,
        // instead of querying the Pilot table a second time per request.
        return new PilotUserDetails(pilot.getEmail(), pilot.getPassword(), authorities, pilot.getTokenVersion());
    }
}
