package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata
import io.github.sanyavertolet.edukate.common.users.UserRole
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("bundles")
data class Bundle(
    @field:Id
    val id: String? = null,
    val name: String,
    val description: String,
    val isPublic: Boolean,
    val problemIds: List<String>,
    val userIdRoleMap: Map<String, UserRole>,
    val invitedUserIds: Set<String> = emptySet(),
    @field:Indexed(unique = true)
    val shareCode: String,
) {
    fun getUserRole(userId: String?): UserRole? = userIdRoleMap[userId]

    fun isUserInBundle(userId: String?): Boolean = getUserRole(userId) != null

    fun isUserInvited(userId: String?): Boolean = userId in invitedUserIds

    fun isAdmin(userId: String): Boolean = UserRole.ADMIN == getUserRole(userId)

    fun getAdminIds(): List<String> = userIdRoleMap.filterValues { it == UserRole.ADMIN }.keys.toList()

    fun withJoinedUser(userId: String, role: UserRole): Bundle = copy(
        userIdRoleMap = userIdRoleMap + (userId to role),
        invitedUserIds = invitedUserIds - userId,
    )

    fun withUserRole(userId: String, role: UserRole): Bundle = copy(userIdRoleMap = userIdRoleMap + (userId to role))

    fun withInvitedUser(userId: String): Bundle = copy(invitedUserIds = invitedUserIds + userId)

    fun withoutInvitedUser(userId: String): Bundle = copy(invitedUserIds = invitedUserIds - userId)

    fun withoutUser(userId: String): Bundle = copy(userIdRoleMap = userIdRoleMap - userId)

    fun withVisibility(isPublic: Boolean): Bundle = copy(isPublic = isPublic)

    fun withProblemIds(problemIds: List<String>): Bundle = copy(problemIds = problemIds.toList())

    fun toDto(problems: List<ProblemMetadata>, admins: List<String>): BundleDto = BundleDto(
        name = name,
        description = description,
        isPublic = isPublic,
        shareCode = shareCode,
        admins = admins,
        problems = problems,
    )

    fun toBundleMetadata(admins: List<String>): BundleMetadata = BundleMetadata(
        name = name,
        description = description,
        shareCode = shareCode,
        isPublic = isPublic,
        size = problemIds.size.toLong(),
        admins = admins
    )

    companion object {
        @JvmStatic
        fun fromCreateRequest(bundleRequest: CreateBundleRequest, adminId: String, shareCode: String) = Bundle(
            name = bundleRequest.name,
            description = bundleRequest.description,
            isPublic = bundleRequest.isPublic,
            problemIds = bundleRequest.problemIds.toList(),
            userIdRoleMap = mapOf(adminId to UserRole.ADMIN),
            shareCode = shareCode,
        )
    }
}
