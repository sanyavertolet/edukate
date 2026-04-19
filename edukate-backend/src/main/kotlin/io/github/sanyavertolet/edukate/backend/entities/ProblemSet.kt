package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.common.users.UserRole
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("problem_sets")
data class ProblemSet(
    @Id val id: Long? = null,
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val shareCode: String,
    val userIdRoleMap: Map<Long, UserRole> = emptyMap(),
    val invitedUserIds: Set<Long> = emptySet(),
) {
    fun getUserRole(userId: Long?): UserRole? = userIdRoleMap[userId]

    fun isUserInProblemSet(userId: Long?): Boolean = getUserRole(userId) != null

    fun isUserInvited(userId: Long?): Boolean = userId in invitedUserIds

    fun isAdmin(userId: Long): Boolean = UserRole.ADMIN == getUserRole(userId)

    fun getAdminIds(): List<Long> = userIdRoleMap.filterValues { it == UserRole.ADMIN }.keys.toList()

    fun withJoinedUser(userId: Long, role: UserRole): ProblemSet =
        copy(userIdRoleMap = userIdRoleMap + (userId to role), invitedUserIds = invitedUserIds - userId)

    fun withUserRole(userId: Long, role: UserRole): ProblemSet = copy(userIdRoleMap = userIdRoleMap + (userId to role))

    fun withInvitedUser(userId: Long): ProblemSet = copy(invitedUserIds = invitedUserIds + userId)

    fun withoutInvitedUser(userId: Long): ProblemSet = copy(invitedUserIds = invitedUserIds - userId)

    fun withoutUser(userId: Long): ProblemSet = copy(userIdRoleMap = userIdRoleMap - userId)

    fun withVisibility(isPublic: Boolean): ProblemSet = copy(isPublic = isPublic)
}
