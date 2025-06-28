package io.github.sanyavertolet.edukate.backend.permissions;

import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import io.github.sanyavertolet.edukate.common.Role;
import org.springframework.stereotype.Component;

@Component
public class BundlePermissionEvaluator {
    public Boolean hasRole(Bundle bundle, String username, Role requiredRole) {
        return bundle.getUserRole(username).compareTo(requiredRole) >= 0;
    }

    public Boolean hasRoleHigherThan(Bundle bundle, String username, Role role) {
        return bundle.getUserRole(username).compareTo(role) > 0;
    }

    public Boolean hasInvitePermission(Bundle bundle, String username) {
        return hasRole(bundle, username, Role.MODERATOR);
    }

    public Boolean hasChangeRolePermission(Bundle bundle, String requesterName, String userName, Role requestedRole) {
        Role currentUserRole = bundle.getUserRole(userName);
        Boolean adminRoleIsHigherThanUserRole = hasRoleHigherThan(bundle, requesterName, currentUserRole);
        Boolean adminRoleIsHigherThanRequestedRole = hasRoleHigherThan(bundle, requesterName, requestedRole);
        return adminRoleIsHigherThanUserRole && adminRoleIsHigherThanRequestedRole;
    }
}
