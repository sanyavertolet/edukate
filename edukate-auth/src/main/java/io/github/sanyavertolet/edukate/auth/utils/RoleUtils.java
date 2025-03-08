package io.github.sanyavertolet.edukate.auth.utils;

import io.github.sanyavertolet.edukate.common.Role;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RoleUtils {
    private RoleUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Set<Role> addRole(Set<Role> roles, Role role) {
        Set<Role> updatedRoles = new HashSet<>(roles);
        updatedRoles.add(role);
        return Collections.unmodifiableSet(updatedRoles);
    }

    public static Set<Role> removeRole(Set<Role> roles, Role role) {
        if (roles.size() <= 1) {
            throw new IllegalStateException("Cannot remove the last role from a user.");
        }

        Set<Role> updatedRoles = new HashSet<>(roles);
        updatedRoles.remove(role);
        return Collections.unmodifiableSet(updatedRoles);
    }

    public static boolean hasRole(Set<Role> roles, Role role) {
        return roles.contains(role);
    }

    public static boolean hasAnyRole(Set<Role> roles, Set<Role> requiredRoles) {
        return requiredRoles.stream().anyMatch(role -> hasRole(roles, role));
    }

    public static Set<Role> getDefaultRole() {
        return Set.of(Role.USER);
    }
}
