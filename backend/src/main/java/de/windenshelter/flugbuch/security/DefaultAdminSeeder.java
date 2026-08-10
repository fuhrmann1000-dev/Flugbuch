package de.windenshelter.flugbuch.security;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import de.windenshelter.flugbuch.model.Pilot;
import de.windenshelter.flugbuch.model.Role;
import de.windenshelter.flugbuch.repository.PilotRepository;
import de.windenshelter.flugbuch.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

/**
 * Makes sure there's always at least one ADMIN pilot account, so there's
 * never a "nobody can manage the app" situation and so developers always
 * have an account to test with. Safe to run on every startup: it only
 * creates the account the first time (see {@link #run}). Runs after
 * {@link DefaultRoleSeeder}, which is what guarantees the ADMIN role exists
 * by the time this runs.
 */
@Component
@RequiredArgsConstructor
@Order(2)
public class DefaultAdminSeeder implements CommandLineRunner {

    private final PilotRepository pilotRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    public void run(String... args) {
        if (pilotRepository.existsByUsername(adminProperties.getUsername())) {
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role is missing - DefaultRoleSeeder should have created it"));

        Pilot admin = new Pilot();
        admin.setUsername(adminProperties.getUsername());
        admin.setPassword(passwordEncoder.encode(adminProperties.getPassword()));
        admin.setRoles(Set.of(adminRole));
        pilotRepository.save(admin);
    }
}
