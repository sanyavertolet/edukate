package io.github.sanyavertolet.edukate.backend.permissions;

import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import io.github.sanyavertolet.edukate.common.Role;
import org.springframework.stereotype.Component;

@Component
public class BundlePermissionEvaluator {
    public Boolean hasRole(Bundle bundle, String username, Role requiredRole) {
        return bundle.getUserRole(username).compareTo(requiredRole) >= 0;
    }

    public Boolean hasInvitePermission(Bundle bundle, String username) {
        return hasRole(bundle, username, Role.MODERATOR);
    }

    public Boolean hasDeletePermission(Bundle bundle, String username) {
        return hasRole(bundle, username, Role.ADMIN);
    }

    public Boolean hasEditPermission(Bundle bundle, String username) {
        return hasRole(bundle, username, Role.MODERATOR);
    }
}
