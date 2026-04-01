package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest
import io.github.sanyavertolet.edukate.common.users.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BundleTest {

    // region isUserInBundle / getUserRole

    @Test
    fun `isUserInBundle returns true when user is in the map`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.USER))
        assertThat(bundle.isUserInBundle("user-1")).isTrue()
    }

    @Test
    fun `isUserInBundle returns false when user is not in the map`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.USER))
        assertThat(bundle.isUserInBundle("user-2")).isFalse()
    }

    @Test
    fun `getUserRole returns the correct role for a member`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN))
        assertThat(bundle.getUserRole("admin-1")).isEqualTo(UserRole.ADMIN)
    }

    @Test
    fun `getUserRole returns null for a non-member`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN))
        assertThat(bundle.getUserRole("nobody")).isNull()
    }

    // endregion

    // region isAdmin / getAdminIds

    @Test
    fun `isAdmin returns true when user has ADMIN role`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN))
        assertThat(bundle.isAdmin("admin-1")).isTrue()
    }

    @Test
    fun `isAdmin returns false when user has USER role`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.USER))
        assertThat(bundle.isAdmin("user-1")).isFalse()
    }

    @Test
    fun `getAdminIds returns only users with ADMIN role`() {
        val bundle =
            BackendFixtures.bundle(
                userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN, "user-1" to UserRole.USER, "admin-2" to UserRole.ADMIN)
            )
        assertThat(bundle.getAdminIds()).containsExactlyInAnyOrder("admin-1", "admin-2")
    }

    @Test
    fun `getAdminIds returns empty list when no admins exist`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.USER))
        assertThat(bundle.getAdminIds()).isEmpty()
    }

    // endregion

    // region invited user management

    @Test
    fun `isUserInvited returns true when user is in invitedUserIds`() {
        val bundle = BackendFixtures.bundle(invitedUserIds = setOf("invited-1"))
        assertThat(bundle.isUserInvited("invited-1")).isTrue()
    }

    @Test
    fun `isUserInvited returns false when user is not invited`() {
        val bundle = BackendFixtures.bundle(invitedUserIds = emptySet())
        assertThat(bundle.isUserInvited("nobody")).isFalse()
    }

    @Test
    fun `withInvitedUser adds user to invitedUserIds`() {
        val bundle = BackendFixtures.bundle(invitedUserIds = emptySet())
        val updated = bundle.withInvitedUser("invited-1")
        assertThat(updated.invitedUserIds).contains("invited-1")
    }

    @Test
    fun `withoutInvitedUser removes user from invitedUserIds`() {
        val bundle = BackendFixtures.bundle(invitedUserIds = setOf("invited-1", "invited-2"))
        val updated = bundle.withoutInvitedUser("invited-1")
        assertThat(updated.invitedUserIds).containsOnly("invited-2")
    }

    // endregion

    // region member mutation

    @Test
    fun `withJoinedUser adds user to userIdRoleMap and removes from invitedUserIds`() {
        val bundle =
            BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN), invitedUserIds = setOf("new-user"))
        val updated = bundle.withJoinedUser("new-user", UserRole.USER)
        assertThat(updated.isUserInBundle("new-user")).isTrue()
        assertThat(updated.getUserRole("new-user")).isEqualTo(UserRole.USER)
        assertThat(updated.isUserInvited("new-user")).isFalse()
    }

    @Test
    fun `withUserRole updates an existing member role`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.USER))
        val updated = bundle.withUserRole("user-1", UserRole.MODERATOR)
        assertThat(updated.getUserRole("user-1")).isEqualTo(UserRole.MODERATOR)
    }

    @Test
    fun `withoutUser removes user from userIdRoleMap`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN, "user-1" to UserRole.USER))
        val updated = bundle.withoutUser("user-1")
        assertThat(updated.isUserInBundle("user-1")).isFalse()
        assertThat(updated.isUserInBundle("admin-1")).isTrue()
    }

    // endregion

    // region visibility / problemIds

    @Test
    fun `withVisibility changes isPublic to true`() {
        val bundle = BackendFixtures.bundle(isPublic = false)
        assertThat(bundle.withVisibility(true).isPublic).isTrue()
    }

    @Test
    fun `withVisibility changes isPublic to false`() {
        val bundle = BackendFixtures.bundle(isPublic = true)
        assertThat(bundle.withVisibility(false).isPublic).isFalse()
    }

    @Test
    fun `withProblemIds replaces the problem list`() {
        val bundle = BackendFixtures.bundle(problemIds = listOf("1.0.0"))
        val updated = bundle.withProblemIds(listOf("2.0.0", "3.0.0"))
        assertThat(updated.problemIds).containsExactly("2.0.0", "3.0.0")
    }

    // endregion

    // region fromCreateRequest

    @Test
    fun `fromCreateRequest sets adminId as the only ADMIN`() {
        val request =
            CreateBundleRequest(name = "My Bundle", description = "Desc", isPublic = true, problemIds = listOf("1.0.0"))
        val bundle = Bundle.fromCreateRequest(request, "admin-1", "CODE123")
        assertThat(bundle.userIdRoleMap).isEqualTo(mapOf("admin-1" to UserRole.ADMIN))
        assertThat(bundle.shareCode).isEqualTo("CODE123")
        assertThat(bundle.isPublic).isTrue()
    }

    // endregion
}
