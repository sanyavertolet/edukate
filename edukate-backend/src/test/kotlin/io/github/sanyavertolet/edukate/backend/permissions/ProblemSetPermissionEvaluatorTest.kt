package io.github.sanyavertolet.edukate.backend.permissions

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.common.users.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProblemSetPermissionEvaluatorTest {

    private lateinit var evaluator: ProblemSetPermissionEvaluator

    @BeforeEach
    fun setUp() {
        evaluator = ProblemSetPermissionEvaluator()
    }

    // region hasRole(problemSet, userId, requiredRole)

    @Test
    fun `hasRole returns true when user role equals required role`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.MODERATOR))
        assertThat(evaluator.hasRole(ps, 1L, UserRole.MODERATOR)).isTrue()
    }

    @Test
    fun `hasRole returns true when user role is higher than required role`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.ADMIN))
        assertThat(evaluator.hasRole(ps, 1L, UserRole.MODERATOR)).isTrue()
    }

    @Test
    fun `hasRole returns false when user role is lower than required role`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.USER))
        assertThat(evaluator.hasRole(ps, 1L, UserRole.MODERATOR)).isFalse()
    }

    @Test
    fun `hasRole returns false when user is not in problem set`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN))
        assertThat(evaluator.hasRole(ps, 999L, UserRole.USER)).isFalse()
    }

    // endregion

    // region hasRole(problemSet, requiredRole, authentication)

    @Test
    fun `hasRole with authentication extracts userId from token`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.ADMIN))
        val auth = BackendFixtures.mockAuthentication(userId = 1L)
        assertThat(evaluator.hasRole(ps, UserRole.MODERATOR, auth)).isTrue()
    }

    // endregion

    // region hasRoleHigherThan

    @Test
    fun `hasRoleHigherThan returns true when user role is strictly above required role`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.ADMIN))
        assertThat(evaluator.hasRoleHigherThan(ps, 1L, UserRole.MODERATOR)).isTrue()
    }

    @Test
    fun `hasRoleHigherThan returns false when user role equals required role`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.MODERATOR))
        assertThat(evaluator.hasRoleHigherThan(ps, 1L, UserRole.MODERATOR)).isFalse()
    }

    @Test
    fun `hasRoleHigherThan returns false when user is not in problem set`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = emptyMap())
        assertThat(evaluator.hasRoleHigherThan(ps, 999L, UserRole.USER)).isFalse()
    }

    // endregion

    // region hasInvitePermission

    @Test
    fun `hasInvitePermission returns true for MODERATOR`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(50L to UserRole.MODERATOR))
        assertThat(evaluator.hasInvitePermission(ps, 50L)).isTrue()
    }

    @Test
    fun `hasInvitePermission returns true for ADMIN`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN))
        assertThat(evaluator.hasInvitePermission(ps, 100L)).isTrue()
    }

    @Test
    fun `hasInvitePermission returns false for plain USER`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.USER))
        assertThat(evaluator.hasInvitePermission(ps, 1L)).isFalse()
    }

    // endregion

    // region hasJoinPermission

    @Test
    fun `hasJoinPermission returns true for public problem set`() {
        val ps = BackendFixtures.problemSet(isPublic = true)
        assertThat(evaluator.hasJoinPermission(ps, 999L)).isTrue()
    }

    @Test
    fun `hasJoinPermission returns true for invited user on private problem set`() {
        val ps = BackendFixtures.problemSet(isPublic = false, invitedUserIds = setOf(200L))
        assertThat(evaluator.hasJoinPermission(ps, 200L)).isTrue()
    }

    @Test
    fun `hasJoinPermission returns false for non-invited user on private problem set`() {
        val ps = BackendFixtures.problemSet(isPublic = false, invitedUserIds = emptySet())
        assertThat(evaluator.hasJoinPermission(ps, 999L)).isFalse()
    }

    // endregion

    // region hasChangeRolePermission

    @Test
    fun `hasChangeRolePermission returns true when requester outranks both current and requested role`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN, 1L to UserRole.USER))
        assertThat(evaluator.hasChangeRolePermission(ps, 100L, 1L, UserRole.MODERATOR)).isTrue()
    }

    @Test
    fun `hasChangeRolePermission returns false when requester equals current role of target`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(50L to UserRole.MODERATOR, 1L to UserRole.MODERATOR))
        assertThat(evaluator.hasChangeRolePermission(ps, 50L, 1L, UserRole.USER)).isFalse()
    }

    @Test
    fun `hasChangeRolePermission returns false when requested role equals requester role`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN, 1L to UserRole.USER))
        assertThat(evaluator.hasChangeRolePermission(ps, 100L, 1L, UserRole.ADMIN)).isFalse()
    }

    @Test
    fun `hasChangeRolePermission returns false when target user is not in problem set`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN))
        assertThat(evaluator.hasChangeRolePermission(ps, 100L, 999L, UserRole.USER)).isFalse()
    }

    // endregion
}
