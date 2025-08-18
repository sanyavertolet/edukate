package io.github.sanyavertolet.edukate.backend.permissions;

import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import io.github.sanyavertolet.edukate.common.Role;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class BundlePermissionEvaluator {
    public Boolean hasRole(Bundle bundle, String userId, Role requiredRole) {
        Role userRole = bundle.getUserRole(userId);
        return userRole != null && userRole.compareTo(requiredRole) >= 0;
    }

    public Boolean hasRole(Bundle bundle, Role requiredRole, Authentication authentication) {
        return hasRole(bundle, AuthUtils.id(authentication), requiredRole);
    }

    public Boolean hasRoleHigherThan(Bundle bundle, String userId, Role requiredRole) {
        Role userRole = bundle.getUserRole(userId);
        return userRole != null  && userRole.compareTo(requiredRole) > 0;
    }

    public Boolean hasInvitePermission(Bundle bundle, String userId) {
        return hasRole(bundle, userId, Role.MODERATOR);
    }

    public Boolean hasJoinPermission(Bundle bundle, String userId) {
        return Boolean.TRUE.equals(bundle.getIsPublic()) || bundle.isUserInvited(userId);
    }

    public Boolean hasChangeRolePermission(Bundle bundle, String requesterId, String userId, Role requestedRole) {
        Role currentUserRole = bundle.getUserRole(userId);
        if (currentUserRole == null) {
            return false;
        }
        boolean requesterRoleIsHigherThanUserRole = hasRoleHigherThan(bundle, requesterId, currentUserRole);
        boolean requesterRoleIsHigherThanRequestedRole = hasRoleHigherThan(bundle, requesterId, requestedRole);
        return requesterRoleIsHigherThanUserRole && requesterRoleIsHigherThanRequestedRole;
    }
}
