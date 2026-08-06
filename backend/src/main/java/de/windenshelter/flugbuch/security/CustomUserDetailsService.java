package de.windenshelter.flugbuch.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.repository.PilotRepository;
import lombok.RequiredArgsConstructor;

/** Loads a {@link Pilot} from the database and adapts it to what Spring Security expects. */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PilotRepository pilotRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Pilot pilot = pilotRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No pilot found with username " + username));

        List<SimpleGrantedAuthority> authorities = pilot.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();

        return new User(pilot.getUsername(), pilot.getPassword(), authorities);
    }
}
