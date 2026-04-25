@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.ChangeProblemSetProblemsRequest
import io.github.sanyavertolet.edukate.backend.dtos.CreateProblemSetRequest
import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetMetadata
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole
import io.github.sanyavertolet.edukate.backend.mappers.ProblemSetMapper
import io.github.sanyavertolet.edukate.backend.permissions.ProblemSetPermissionEvaluator
import io.github.sanyavertolet.edukate.backend.services.ProblemSetService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(ProblemSetController::class)
@Import(NoopWebSecurityConfig::class)
class ProblemSetControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var problemSetService: ProblemSetService
    @MockkBean private lateinit var problemSetMapper: ProblemSetMapper
    @MockkBean private lateinit var userService: UserService
    @MockkBean private lateinit var notifier: Notifier
    @MockkBean private lateinit var problemSetPermissionEvaluator: ProblemSetPermissionEvaluator

    private fun authenticatedClient(): WebTestClient =
        webTestClient.mutateWith(
            SecurityMockServerConfigurers.mockAuthentication(BackendFixtures.mockAuthentication(userId = 1L))
        )

    private fun psDto(shareCode: String = "SHARE123") =
        ProblemSetDto("Test ProblemSet", "Description", listOf("admin-1"), false, emptyList(), shareCode)

    private fun psMetadata(shareCode: String = "SHARE123") =
        ProblemSetMetadata("Test ProblemSet", "Description", listOf("admin-1"), shareCode, false, 1L, 0L)

    // region POST /api/v1/problem-sets

    @Test
    fun `createProblemSet returns 200 with problem set DTO`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.createProblemSet(any(), any()) } returns Mono.just(ps)
        every { problemSetMapper.toDto(ps, any()) } returns Mono.just(psDto())

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(CreateProblemSetRequest("Test ProblemSet", "Description", false, listOf("savchenko/P1")))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.shareCode")
            .isEqualTo("SHARE123")
            .jsonPath("$.name")
            .isEqualTo("Test ProblemSet")
    }

    // endregion

    // region GET /api/v1/problem-sets/public

    @Test
    fun `getPublicProblemSets returns 200 with metadata list (no auth required)`() {
        val ps = BackendFixtures.problemSet(isPublic = true)
        every { problemSetService.getPublicProblemSets(any()) } returns Flux.just(ps)
        every { problemSetMapper.toMetadata(ps, any()) } returns Mono.just(psMetadata())

        webTestClient
            .get()
            .uri("/api/v1/problem-sets/public")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemSetMetadata>()
            .hasSize(1)
    }

    // endregion

    // region GET /api/v1/problem-sets/owned

    @Test
    fun `getOwnedProblemSets returns 200 with owned metadata`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.getOwnedProblemSets(any(), any()) } returns Flux.just(ps)
        every { problemSetMapper.toMetadata(ps, any()) } returns Mono.just(psMetadata())

        authenticatedClient()
            .get()
            .uri("/api/v1/problem-sets/owned")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemSetMetadata>()
            .hasSize(1)
    }

    // endregion

    // region GET /api/v1/problem-sets/{shareCode}

    @Test
    fun `getProblemSetByShareCode returns 200 when user is a member`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(1L to UserRole.USER))
        every { problemSetService.findByShareCode("SHARE123") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasReadPermission(ps, 1L) } returns true
        every { problemSetMapper.toDto(ps, any()) } returns Mono.just(psDto())

        authenticatedClient()
            .get()
            .uri("/api/v1/problem-sets/SHARE123")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.shareCode")
            .isEqualTo("SHARE123")
    }

    @Test
    fun `getProblemSetByShareCode returns 403 when user is not a member`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN))
        every { problemSetService.findByShareCode("SHARE123") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasReadPermission(ps, 1L) } returns false

        authenticatedClient().get().uri("/api/v1/problem-sets/SHARE123").exchange().expectStatus().isForbidden
    }

    // endregion

    // region GET /api/v1/problem-sets/joined

    @Test
    fun `getJoinedProblemSets returns 200 with joined metadata`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.getJoinedProblemSets(any(), any()) } returns Flux.just(ps)
        every { problemSetMapper.toMetadata(ps, any()) } returns Mono.just(psMetadata())

        authenticatedClient()
            .get()
            .uri("/api/v1/problem-sets/joined")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemSetMetadata>()
            .hasSize(1)
    }

    // endregion

    // region POST /api/v1/problem-sets/{shareCode}/leave

    @Test
    fun `leaveProblemSet returns 200 with shareCode`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.removeUser("SHARE123", 1L) } returns Mono.just(ps)

        authenticatedClient().post().uri("/api/v1/problem-sets/SHARE123/leave").exchange().expectStatus().isOk
    }

    @Test
    fun `leaveProblemSet returns 400 when last admin`() {
        every { problemSetService.removeUser("SHARE123", 1L) } returns
            Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove last admin"))

        authenticatedClient().post().uri("/api/v1/problem-sets/SHARE123/leave").exchange().expectStatus().isBadRequest
    }

    // endregion

    // region POST /api/v1/problem-sets/{shareCode}/invite

    @Test
    fun `inviteToProblemSet returns 200 success message`() {
        val invitee = BackendFixtures.user(id = 2L, name = "invitee")
        val ps = BackendFixtures.problemSet()
        every { userService.findUserByName("invitee") } returns Mono.just(invitee)
        every { problemSetService.inviteUser("SHARE123", 1L, 2L) } returns Mono.just(ps)
        every { notifier.notify(any()) } returns Mono.just("notification-id")

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/invite?inviteeName=invitee")
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `inviteToProblemSet returns 403 when requester lacks invite permission`() {
        val invitee = BackendFixtures.user(id = 2L, name = "invitee")
        every { userService.findUserByName("invitee") } returns Mono.just(invitee)
        every { problemSetService.inviteUser("SHARE123", 1L, 2L) } returns
            Mono.error(ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions"))

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/invite?inviteeName=invitee")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    // endregion

    // region POST /api/v1/problem-sets/{shareCode}/expire-invite

    @Test
    fun `expireInvite returns 200 success message`() {
        val invitee = BackendFixtures.user(id = 2L, name = "invitee")
        val ps = BackendFixtures.problemSet()
        every { userService.findUserByName("invitee") } returns Mono.just(invitee)
        every { problemSetService.expireInvite("SHARE123", 1L, 2L) } returns Mono.just(ps)

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/expire-invite?inviteeName=invitee")
            .exchange()
            .expectStatus()
            .isOk
    }

    // endregion

    // region POST /api/v1/problem-sets/{shareCode}/reply-invite

    @Test
    fun `replyToInvite accept returns 200 success message`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.reactToInvite("SHARE123", true, any()) } returns Mono.just(ps)

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/reply-invite?response=true")
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `replyToInvite decline returns 200 success message`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.reactToInvite("SHARE123", false, any()) } returns Mono.just(ps)

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/reply-invite?response=false")
            .exchange()
            .expectStatus()
            .isOk
    }

    // endregion

    // region GET /api/v1/problem-sets/{shareCode}/users

    @Test
    fun `getUserRoles returns 200 with user role list`() {
        val ps = BackendFixtures.problemSet(shareCode = "SHARE123")
        every { problemSetService.getProblemSetForModerator("SHARE123", any()) } returns Mono.just(ps)
        every { problemSetMapper.toUserRoles(ps) } returns Flux.just(UserNameWithRole("admin-1", UserRole.ADMIN))

        authenticatedClient()
            .get()
            .uri("/api/v1/problem-sets/SHARE123/users")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<UserNameWithRole>()
            .hasSize(1)
    }

    // endregion

    // region GET /api/v1/problem-sets/{shareCode}/invited-users

    @Test
    fun `getInvitedUsers returns 200 with invited user list`() {
        val ps = BackendFixtures.problemSet(shareCode = "SHARE123")
        every { problemSetService.getProblemSetForModerator("SHARE123", any()) } returns Mono.just(ps)
        every { problemSetMapper.toInvitedUserNames(ps) } returns Flux.just("invitee-1")

        authenticatedClient()
            .get()
            .uri("/api/v1/problem-sets/SHARE123/invited-users")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<String>()
            .hasSize(1)
    }

    // endregion

    // region POST /api/v1/problem-sets/{shareCode}/role

    @Test
    fun `changeUserRole returns 200 with updated role`() {
        val targetUser = BackendFixtures.user(id = 2L, name = "target")
        every { userService.findUserByName("target") } returns Mono.just(targetUser)
        every { problemSetService.changeUserRole("SHARE123", 2L, UserRole.MODERATOR, any()) } returns
            Mono.just(UserRole.MODERATOR)

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/role?username=target&requestedRole=MODERATOR")
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `changeUserRole returns 403 when requester lacks permission`() {
        val targetUser = BackendFixtures.user(id = 2L, name = "target")
        every { userService.findUserByName("target") } returns Mono.just(targetUser)
        every { problemSetService.changeUserRole("SHARE123", 2L, UserRole.ADMIN, any()) } returns
            Mono.error(ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions"))

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/role?username=target&requestedRole=ADMIN")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    // endregion

    // region POST /api/v1/problem-sets/{shareCode}/visibility

    @Test
    fun `changeVisibility returns 200 with updated problem set DTO`() {
        val ps = BackendFixtures.problemSet(isPublic = true)
        every { problemSetService.changeVisibility("SHARE123", true, any()) } returns Mono.just(ps)
        every { problemSetMapper.toDto(ps, any()) } returns Mono.just(psDto())

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/visibility?isPublic=true")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.shareCode")
            .isEqualTo("SHARE123")
    }

    // endregion

    // region POST /api/v1/problem-sets/{shareCode}/problems

    @Test
    fun `changeProblems returns 200 with updated problem set DTO`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.changeProblems("SHARE123", listOf("savchenko/P1", "savchenko/P2"), any()) } returns
            Mono.just(ps)
        every { problemSetMapper.toDto(ps, any()) } returns Mono.just(psDto())

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/problems")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ChangeProblemSetProblemsRequest(listOf("savchenko/P1", "savchenko/P2")))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.shareCode")
            .isEqualTo("SHARE123")
    }

    @Test
    fun `changeProblems returns 400 when problem list is empty`() {
        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/problems")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ChangeProblemSetProblemsRequest(problemKeys = emptyList()))
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    // endregion
}
