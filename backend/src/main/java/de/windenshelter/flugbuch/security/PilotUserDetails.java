package de.windenshelter.flugbuch.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;

/**
 * Spring Security's plain {@link User}, plus the pilot's {@code tokenVersion}.
 * {@link CustomUserDetailsService} already loads the {@code Pilot} row from
 * the database to build a {@link User} on every authenticated request (for
 * {@link JwtAuthenticationFilter}) and on every login (for the
 * {@code AuthenticationManager}); carrying tokenVersion along on that same
 * object lets both call sites check it without a second database query.
 */
@Getter
public class PilotUserDetails extends User {

    private final int tokenVersion;

    public PilotUserDetails(String username, String password,
            Collection<? extends GrantedAuthority> authorities, int tokenVersion) {
        super(username, password, authorities);
        this.tokenVersion = tokenVersion;
    }
}
