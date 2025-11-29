package io.github.sanyavertolet.edukate.common.users;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum UserRole {
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

    public static Set<UserRole> fromString(String rolesString) {
        if (rolesString == null || rolesString.trim().isEmpty()) {
            return EnumSet.noneOf(UserRole.class);
        }
        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .map(UserRole::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(UserRole.class)));
    }

    public static String toString(Collection<UserRole> roles) {
        return roles.stream()
                .map(UserRole::name)
                .collect(Collectors.joining(","));
    }

    public static Collection<UserRole> anyRole() {
        return EnumSet.allOf(UserRole.class);
    }
}
