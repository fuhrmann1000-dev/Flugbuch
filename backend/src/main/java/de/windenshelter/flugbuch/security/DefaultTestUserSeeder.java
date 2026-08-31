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
 * Makes sure there's always a plain, non-ADMIN pilot account too - so
 * whoever is testing the app (e.g. the ADMIN-only parts of the Helpers view,
 * ticket #54) can log in as a regular pilot and actually see the
 * difference, instead of only ever having the always-privileged seeded
 * admin account to try things with. Same lifecycle as {@link DefaultAdminSeeder}:
 * safe to run on every startup, only creates the account the first time.
 * Runs after {@link DefaultRoleSeeder} (needs the USER role to exist).
 *
 * Unlike the admin account, these credentials are NOT sourced from
 * environment variables - this is a local dev/testing convenience only, not
 * meant to be configured per deployment. Worth reconsidering (or disabling
 * outright) before this app is ever exposed somewhere that isn't a trusted
 * local/dev environment.
 */
@Component
@RequiredArgsConstructor
@Order(3)
public class DefaultTestUserSeeder implements CommandLineRunner {

    private static final String TEST_USER_EMAIL = "pilot@flugbuch.local";
    private static final String TEST_USER_PASSWORD = "TestPilot123";

    private final PilotRepository pilotRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (pilotRepository.existsByEmail(TEST_USER_EMAIL)) {
            return;
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER role is missing - DefaultRoleSeeder should have created it"));

        Pilot testPilot = new Pilot();
        testPilot.setUsername("Test Pilot");
        testPilot.setPassword(passwordEncoder.encode(TEST_USER_PASSWORD));
        testPilot.setRoles(Set.of(userRole));

        // Placeholder profile data, same reasoning as DefaultAdminSeeder's:
        // GET /pilots/me has something real to show right away.
        testPilot.setFirstName("Test");
        testPilot.setLastName("Pilot");
        testPilot.setEmail(TEST_USER_EMAIL);
        testPilot.setPhone("+49 000 0000001");
        testPilot.setLicenseType("PPL(A)");
        testPilot.setLicenseNumber("D.PPL.00002");
        testPilot.setHomeAirfield("EDPU — Altes Lager");

        pilotRepository.save(testPilot);
    }
}
