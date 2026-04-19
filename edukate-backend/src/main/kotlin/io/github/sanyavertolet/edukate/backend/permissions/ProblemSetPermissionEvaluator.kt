package io.github.sanyavertolet.edukate.backend.permissions

import io.github.sanyavertolet.edukate.backend.entities.ProblemSet
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.utils.id
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class ProblemSetPermissionEvaluator {
    fun hasRole(problemSet: ProblemSet, userId: Long, requiredRole: UserRole): Boolean {
        val userRole = problemSet.getUserRole(userId)
        return userRole != null && userRole >= requiredRole
    }

    fun hasRole(problemSet: ProblemSet, requiredRole: UserRole, authentication: Authentication?): Boolean =
        authentication?.id()?.let { hasRole(problemSet, it, requiredRole) } ?: false

    fun hasRoleHigherThan(problemSet: ProblemSet, userId: Long, requiredRole: UserRole): Boolean {
        val userRole = problemSet.getUserRole(userId)
        return userRole != null && userRole > requiredRole
    }

    fun hasInvitePermission(problemSet: ProblemSet, userId: Long): Boolean = hasRole(problemSet, userId, UserRole.MODERATOR)

    fun hasReadPermission(problemSet: ProblemSet, userId: Long?): Boolean =
        problemSet.isPublic || problemSet.isUserInProblemSet(userId)

    fun hasJoinPermission(problemSet: ProblemSet, userId: Long): Boolean =
        problemSet.isPublic || problemSet.isUserInvited(userId)

    fun hasChangeRolePermission(problemSet: ProblemSet, requesterId: Long, userId: Long, requestedRole: UserRole): Boolean {
        val currentUserRole = problemSet.getUserRole(userId) ?: return false
        val requesterRoleIsHigherThanUserRole = hasRoleHigherThan(problemSet, requesterId, currentUserRole)
        val requesterRoleIsHigherThanRequestedRole = hasRoleHigherThan(problemSet, requesterId, requestedRole)
        return requesterRoleIsHigherThanUserRole && requesterRoleIsHigherThanRequestedRole
    }
}
