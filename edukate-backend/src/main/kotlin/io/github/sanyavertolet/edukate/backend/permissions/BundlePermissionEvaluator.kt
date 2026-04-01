package io.github.sanyavertolet.edukate.backend.permissions

import io.github.sanyavertolet.edukate.backend.entities.Bundle
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.utils.id
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class BundlePermissionEvaluator {
    fun hasRole(bundle: Bundle, userId: String, requiredRole: UserRole): Boolean {
        val userRole = bundle.getUserRole(userId)
        return userRole != null && userRole >= requiredRole
    }

    fun hasRole(bundle: Bundle, requiredRole: UserRole, authentication: Authentication?): Boolean =
        authentication?.id()?.let { hasRole(bundle, it, requiredRole) } ?: false

    fun hasRoleHigherThan(bundle: Bundle, userId: String, requiredRole: UserRole): Boolean {
        val userRole = bundle.getUserRole(userId)
        return userRole != null && userRole > requiredRole
    }

    fun hasInvitePermission(bundle: Bundle, userId: String): Boolean = hasRole(bundle, userId, UserRole.MODERATOR)

    fun hasJoinPermission(bundle: Bundle, userId: String): Boolean = bundle.isPublic || bundle.isUserInvited(userId)

    fun hasChangeRolePermission(bundle: Bundle, requesterId: String, userId: String, requestedRole: UserRole): Boolean {
        val currentUserRole = bundle.getUserRole(userId) ?: return false
        val requesterRoleIsHigherThanUserRole = hasRoleHigherThan(bundle, requesterId, currentUserRole)
        val requesterRoleIsHigherThanRequestedRole = hasRoleHigherThan(bundle, requesterId, requestedRole)
        return requesterRoleIsHigherThanUserRole && requesterRoleIsHigherThanRequestedRole
    }
}
