package de.windenshelter.flugbuch.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import de.windenshelter.flugbuch.model.Role;
import de.windenshelter.flugbuch.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

/**
 * Makes sure the base roles (USER, ADMIN) exist in the database on startup,
 * so pilot registration always has a default role to assign. Safe to run on
 * every startup: it only inserts a role if it isn't already there.
 * Runs before {@link DefaultAdminSeeder}, which needs the ADMIN role to
 * already exist.
 */
@Component
@RequiredArgsConstructor
@Order(1)
public class DefaultRoleSeeder implements CommandLineRunner {

    private static final String[] DEFAULT_ROLES = {"USER", "ADMIN"};

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        for (String roleName : DEFAULT_ROLES) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
            }
        }
    }
}
