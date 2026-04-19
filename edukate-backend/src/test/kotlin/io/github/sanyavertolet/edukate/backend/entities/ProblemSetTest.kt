package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.common.users.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProblemSetTest {

    // region isUserInProblemSet / getUserRole

    @Test
    fun `isUserInProblemSet returns true when user is in the map`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.USER))
        assertThat(ps.isUserInProblemSet(1L)).isTrue()
    }

    @Test
    fun `isUserInProblemSet returns false when user is not in the map`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.USER))
        assertThat(ps.isUserInProblemSet(2L)).isFalse()
    }

    @Test
    fun `getUserRole returns the correct role for a member`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN))
        assertThat(ps.getUserRole(100L)).isEqualTo(UserRole.ADMIN)
    }

    @Test
    fun `getUserRole returns null for a non-member`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN))
        assertThat(ps.getUserRole(999L)).isNull()
    }

    // endregion

    // region isAdmin / getAdminIds

    @Test
    fun `isAdmin returns true when user has ADMIN role`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN))
        assertThat(ps.isAdmin(100L)).isTrue()
    }

    @Test
    fun `isAdmin returns false when user has USER role`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.USER))
        assertThat(ps.isAdmin(1L)).isFalse()
    }

    @Test
    fun `getAdminIds returns only users with ADMIN role`() {
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN, 1L to UserRole.USER, 101L to UserRole.ADMIN)
            )
        assertThat(ps.getAdminIds()).containsExactlyInAnyOrder(100L, 101L)
    }

    @Test
    fun `getAdminIds returns empty list when no admins exist`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.USER))
        assertThat(ps.getAdminIds()).isEmpty()
    }

    // endregion

    // region invited user management

    @Test
    fun `isUserInvited returns true when user is in invitedUserIds`() {
        val ps = BackendFixtures.problemSet(invitedUserIds = setOf(200L))
        assertThat(ps.isUserInvited(200L)).isTrue()
    }

    @Test
    fun `isUserInvited returns false when user is not invited`() {
        val ps = BackendFixtures.problemSet(invitedUserIds = emptySet())
        assertThat(ps.isUserInvited(999L)).isFalse()
    }

    @Test
    fun `withInvitedUser adds user to invitedUserIds`() {
        val ps = BackendFixtures.problemSet(invitedUserIds = emptySet())
        val updated = ps.withInvitedUser(200L)
        assertThat(updated.invitedUserIds).contains(200L)
    }

    @Test
    fun `withoutInvitedUser removes user from invitedUserIds`() {
        val ps = BackendFixtures.problemSet(invitedUserIds = setOf(200L, 201L))
        val updated = ps.withoutInvitedUser(200L)
        assertThat(updated.invitedUserIds).containsOnly(201L)
    }

    // endregion

    // region member mutation

    @Test
    fun `withJoinedUser adds user to userIdRoleMap and removes from invitedUserIds`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN), invitedUserIds = setOf(300L))
        val updated = ps.withJoinedUser(300L, UserRole.USER)
        assertThat(updated.isUserInProblemSet(300L)).isTrue()
        assertThat(updated.getUserRole(300L)).isEqualTo(UserRole.USER)
        assertThat(updated.isUserInvited(300L)).isFalse()
    }

    @Test
    fun `withUserRole updates an existing member role`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.USER))
        val updated = ps.withUserRole(1L, UserRole.MODERATOR)
        assertThat(updated.getUserRole(1L)).isEqualTo(UserRole.MODERATOR)
    }

    @Test
    fun `withoutUser removes user from userIdRoleMap`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN, 1L to UserRole.USER))
        val updated = ps.withoutUser(1L)
        assertThat(updated.isUserInProblemSet(1L)).isFalse()
        assertThat(updated.isUserInProblemSet(100L)).isTrue()
    }

    // endregion

    // region visibility

    @Test
    fun `withVisibility changes isPublic to true`() {
        val ps = BackendFixtures.problemSet(isPublic = false)
        assertThat(ps.withVisibility(true).isPublic).isTrue()
    }

    @Test
    fun `withVisibility changes isPublic to false`() {
        val ps = BackendFixtures.problemSet(isPublic = true)
        assertThat(ps.withVisibility(false).isPublic).isFalse()
    }

    // endregion
}
