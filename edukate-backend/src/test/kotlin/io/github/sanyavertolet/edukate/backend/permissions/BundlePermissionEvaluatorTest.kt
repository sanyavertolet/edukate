package io.github.sanyavertolet.edukate.backend.permissions

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.common.users.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BundlePermissionEvaluatorTest {

    private lateinit var evaluator: BundlePermissionEvaluator

    @BeforeEach
    fun setUp() {
        evaluator = BundlePermissionEvaluator()
    }

    // region hasRole(bundle, userId, requiredRole)

    @Test
    fun `hasRole returns true when user role equals required role`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.MODERATOR))
        assertThat(evaluator.hasRole(bundle, "user-1", UserRole.MODERATOR)).isTrue()
    }

    @Test
    fun `hasRole returns true when user role is higher than required role`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.ADMIN))
        assertThat(evaluator.hasRole(bundle, "user-1", UserRole.MODERATOR)).isTrue()
    }

    @Test
    fun `hasRole returns false when user role is lower than required role`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.USER))
        assertThat(evaluator.hasRole(bundle, "user-1", UserRole.MODERATOR)).isFalse()
    }

    @Test
    fun `hasRole returns false when user is not in bundle`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN))
        assertThat(evaluator.hasRole(bundle, "nobody", UserRole.USER)).isFalse()
    }

    // endregion

    // region hasRole(bundle, requiredRole, authentication)

    @Test
    fun `hasRole with authentication extracts userId from token`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.ADMIN))
        val auth = BackendFixtures.mockAuthentication(userId = "user-1")
        assertThat(evaluator.hasRole(bundle, UserRole.MODERATOR, auth)).isTrue()
    }

    // endregion

    // region hasRoleHigherThan

    @Test
    fun `hasRoleHigherThan returns true when user role is strictly above required role`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.ADMIN))
        assertThat(evaluator.hasRoleHigherThan(bundle, "user-1", UserRole.MODERATOR)).isTrue()
    }

    @Test
    fun `hasRoleHigherThan returns false when user role equals required role`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.MODERATOR))
        assertThat(evaluator.hasRoleHigherThan(bundle, "user-1", UserRole.MODERATOR)).isFalse()
    }

    @Test
    fun `hasRoleHigherThan returns false when user is not in bundle`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = emptyMap())
        assertThat(evaluator.hasRoleHigherThan(bundle, "nobody", UserRole.USER)).isFalse()
    }

    // endregion

    // region hasInvitePermission

    @Test
    fun `hasInvitePermission returns true for MODERATOR`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("mod-1" to UserRole.MODERATOR))
        assertThat(evaluator.hasInvitePermission(bundle, "mod-1")).isTrue()
    }

    @Test
    fun `hasInvitePermission returns true for ADMIN`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN))
        assertThat(evaluator.hasInvitePermission(bundle, "admin-1")).isTrue()
    }

    @Test
    fun `hasInvitePermission returns false for plain USER`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.USER))
        assertThat(evaluator.hasInvitePermission(bundle, "user-1")).isFalse()
    }

    // endregion

    // region hasJoinPermission

    @Test
    fun `hasJoinPermission returns true for public bundle`() {
        val bundle = BackendFixtures.bundle(isPublic = true)
        assertThat(evaluator.hasJoinPermission(bundle, "any-user")).isTrue()
    }

    @Test
    fun `hasJoinPermission returns true for invited user on private bundle`() {
        val bundle = BackendFixtures.bundle(isPublic = false, invitedUserIds = setOf("invited-1"))
        assertThat(evaluator.hasJoinPermission(bundle, "invited-1")).isTrue()
    }

    @Test
    fun `hasJoinPermission returns false for non-invited user on private bundle`() {
        val bundle = BackendFixtures.bundle(isPublic = false, invitedUserIds = emptySet())
        assertThat(evaluator.hasJoinPermission(bundle, "stranger")).isFalse()
    }

    // endregion

    // region hasChangeRolePermission

    @Test
    fun `hasChangeRolePermission returns true when requester outranks both current and requested role`() {
        // admin-1 is ADMIN; user-1 is USER; requesting MODERATOR for user-1
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN, "user-1" to UserRole.USER))
        assertThat(evaluator.hasChangeRolePermission(bundle, "admin-1", "user-1", UserRole.MODERATOR)).isTrue()
    }

    @Test
    fun `hasChangeRolePermission returns false when requester equals current role of target`() {
        // mod-1 is MODERATOR; user-1 is MODERATOR; ADMIN cannot be granted because requester is not
        // higher
        val bundle =
            BackendFixtures.bundle(userIdRoleMap = mapOf("mod-1" to UserRole.MODERATOR, "user-1" to UserRole.MODERATOR))
        assertThat(evaluator.hasChangeRolePermission(bundle, "mod-1", "user-1", UserRole.USER)).isFalse()
    }

    @Test
    fun `hasChangeRolePermission returns false when requested role equals requester role`() {
        // admin-1 is ADMIN; user-1 is USER; trying to promote user-1 to ADMIN — requester is not
        // strictly higher than ADMIN
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN, "user-1" to UserRole.USER))
        assertThat(evaluator.hasChangeRolePermission(bundle, "admin-1", "user-1", UserRole.ADMIN)).isFalse()
    }

    @Test
    fun `hasChangeRolePermission returns false when target user is not in bundle`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN))
        assertThat(evaluator.hasChangeRolePermission(bundle, "admin-1", "nobody", UserRole.USER)).isFalse()
    }

    // endregion
}
