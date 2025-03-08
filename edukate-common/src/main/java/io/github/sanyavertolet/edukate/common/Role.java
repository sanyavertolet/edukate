package io.github.sanyavertolet.edukate.common;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    USER,
    MODERATOR,
    ADMIN,
    ;

    private static final String ROLE_PREFIX = "ROLE_";

    public String asSpringSecurityRole() {
        return ROLE_PREFIX + name();
    }

    public SimpleGrantedAuthority asGrantedAuthority() {
        return new SimpleGrantedAuthority(asSpringSecurityRole());
    }

    public static Set<Role> fromString(String rolesString) {
        if (rolesString == null || rolesString.trim().isEmpty()) {
            return EnumSet.noneOf(Role.class);
        }
        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .map(Role::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
    }

    public static String toString(Collection<Role> roles) {
        return roles.stream()
                .map(Role::name)
                .collect(Collectors.joining(","));
    }
}
