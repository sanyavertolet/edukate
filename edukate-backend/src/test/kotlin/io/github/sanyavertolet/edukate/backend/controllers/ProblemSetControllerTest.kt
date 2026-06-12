@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.CreateInvitationRequest
import io.github.sanyavertolet.edukate.backend.dtos.CreateProblemSetRequest
import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetMetadata
import io.github.sanyavertolet.edukate.backend.dtos.SetMemberRoleRequest
import io.github.sanyavertolet.edukate.backend.dtos.UpdateProblemSetSettingsRequest
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole
import io.github.sanyavertolet.edukate.backend.mappers.ProblemSetMapper
import io.github.sanyavertolet.edukate.backend.permissions.ProblemSetPermissionEvaluator
import io.github.sanyavertolet.edukate.backend.services.ProblemSetService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
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
    @MockkBean private lateinit var problemSetPermissionEvaluator: ProblemSetPermissionEvaluator

    private fun authenticatedClient(): WebTestClient =
        webTestClient.mutateWith(
            SecurityMockServerConfigurers.mockAuthentication(BackendFixtures.mockAuthentication(userId = 1L))
        )

    private fun psDto(shareCode: String = "SHARE123") =
        ProblemSetDto("Test ProblemSet", "Description", listOf("admin-1"), emptyList(), false, emptyList(), shareCode)

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

    // region GET /api/v1/problem-sets/member

    @Test
    fun `getMemberProblemSets returns 200 with member metadata`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.getMemberProblemSets(any(), any(), any()) } returns Flux.just(ps)
        every { problemSetMapper.toMetadata(ps, any()) } returns Mono.just(psMetadata())

        authenticatedClient()
            .get()
            .uri("/api/v1/problem-sets/member")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemSetMetadata>()
            .hasSize(1)
    }

    @Test
    fun `getMemberProblemSets passes role filter through to service`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.getMemberProblemSets(any(), any(), any()) } returns Flux.just(ps)
        every { problemSetMapper.toMetadata(ps, any()) } returns Mono.just(psMetadata())

        authenticatedClient().get().uri("/api/v1/problem-sets/member?roles=ADMIN").exchange().expectStatus().isOk
    }

    // endregion

    // region GET /api/v1/problem-sets/search/member

    @Test
    fun `searchMemberProblemSets returns 200 with matching problem sets`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.searchMemberProblemSets(any(), any(), any(), any()) } returns Flux.just(ps)
        every { problemSetMapper.toMetadata(ps, any()) } returns Mono.just(psMetadata())

        authenticatedClient()
            .get()
            .uri("/api/v1/problem-sets/search/member?query=Test")
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
    fun `getProblemSetByShareCode returns 404 when user is not a member (no existence leak)`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN))
        every { problemSetService.findByShareCode("SHARE123") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasReadPermission(ps, 1L) } returns false

        authenticatedClient().get().uri("/api/v1/problem-sets/SHARE123").exchange().expectStatus().isNotFound
    }

    // endregion

    // region PATCH /api/v1/problem-sets/{shareCode}

    @Test
    fun `updateSettings returns 200 with updated DTO`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.updateSettings("SHARE123", any(), any()) } returns Mono.just(ps)
        every { problemSetMapper.toDto(ps, any()) } returns Mono.just(psDto())

        authenticatedClient()
            .patch()
            .uri("/api/v1/problem-sets/SHARE123")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(UpdateProblemSetSettingsRequest(name = "New Name"))
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `updateSettings returns 400 when body is empty (no fields provided)`() {
        authenticatedClient()
            .patch()
            .uri("/api/v1/problem-sets/SHARE123")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(UpdateProblemSetSettingsRequest())
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `updateSettings returns 400 when problemKeys is empty list`() {
        authenticatedClient()
            .patch()
            .uri("/api/v1/problem-sets/SHARE123")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(UpdateProblemSetSettingsRequest(problemKeys = emptyList()))
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `updateSettings returns 403 when service throws FORBIDDEN`() {
        every { problemSetService.updateSettings("SHARE123", any(), any()) } returns
            Mono.error(ResponseStatusException(HttpStatus.FORBIDDEN, "Moderator access required"))

        authenticatedClient()
            .patch()
            .uri("/api/v1/problem-sets/SHARE123")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(UpdateProblemSetSettingsRequest(isPublic = true))
            .exchange()
            .expectStatus()
            .isForbidden
    }

    // endregion

    // region GET /api/v1/problem-sets/{shareCode}/members

    @Test
    fun `getMembers returns 200 with role list`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.loadForModerator("SHARE123", any()) } returns Mono.just(ps)
        every { problemSetMapper.toUserRoles(ps) } returns Flux.just(UserNameWithRole("admin-1", UserRole.ADMIN))

        authenticatedClient()
            .get()
            .uri("/api/v1/problem-sets/SHARE123/members")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<UserNameWithRole>()
            .hasSize(1)
    }

    // endregion

    // region PUT /api/v1/problem-sets/{shareCode}/members/{username}

    @Test
    fun `setMemberRole returns 200 with updated DTO`() {
        val targetUser = BackendFixtures.user(id = 2L, name = "target")
        val ps = BackendFixtures.problemSet()
        every { userService.findUserByName("target") } returns Mono.just(targetUser)
        every { problemSetService.setMemberRole("SHARE123", 2L, UserRole.MODERATOR, any()) } returns Mono.just(ps)
        every { problemSetMapper.toDto(ps, any()) } returns Mono.just(psDto())

        authenticatedClient()
            .put()
            .uri("/api/v1/problem-sets/SHARE123/members/target")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(SetMemberRoleRequest(UserRole.MODERATOR))
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `setMemberRole returns 400 when demoting the last admin`() {
        val targetUser = BackendFixtures.user(id = 100L, name = "lone-admin")
        every { userService.findUserByName("lone-admin") } returns Mono.just(targetUser)
        every { problemSetService.setMemberRole("SHARE123", 100L, UserRole.USER, any()) } returns
            Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot demote the last admin"))

        authenticatedClient()
            .put()
            .uri("/api/v1/problem-sets/SHARE123/members/lone-admin")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(SetMemberRoleRequest(UserRole.USER))
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `setMemberRole returns 404 when target user does not exist`() {
        every { userService.findUserByName("ghost") } returns Mono.empty()

        authenticatedClient()
            .put()
            .uri("/api/v1/problem-sets/SHARE123/members/ghost")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(SetMemberRoleRequest(UserRole.USER))
            .exchange()
            .expectStatus()
            .isNotFound
    }

    // endregion

    // region DELETE /api/v1/problem-sets/{shareCode}/members/{username}

    @Test
    fun `removeMember returns 200 with updated DTO`() {
        val targetUser = BackendFixtures.user(id = 2L, name = "target")
        val ps = BackendFixtures.problemSet()
        every { userService.findUserByName("target") } returns Mono.just(targetUser)
        every { problemSetService.removeMember("SHARE123", 2L, any()) } returns Mono.just(ps)
        every { problemSetMapper.toDto(ps, any()) } returns Mono.just(psDto())

        authenticatedClient().delete().uri("/api/v1/problem-sets/SHARE123/members/target").exchange().expectStatus().isOk
    }

    @Test
    fun `removeMember returns 400 when removing the last admin`() {
        val targetUser = BackendFixtures.user(id = 100L, name = "lone-admin")
        every { userService.findUserByName("lone-admin") } returns Mono.just(targetUser)
        every { problemSetService.removeMember("SHARE123", 100L, any()) } returns
            Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove the last admin"))

        authenticatedClient()
            .delete()
            .uri("/api/v1/problem-sets/SHARE123/members/lone-admin")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    // endregion

    // region DELETE /api/v1/problem-sets/{shareCode}/members/me

    @Test
    fun `leaveProblemSet returns 204`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.leaveProblemSet("SHARE123", any()) } returns Mono.just(ps)

        authenticatedClient().delete().uri("/api/v1/problem-sets/SHARE123/members/me").exchange().expectStatus().isNoContent
    }

    @Test
    fun `leaveProblemSet returns 400 when caller is the last admin`() {
        every { problemSetService.leaveProblemSet("SHARE123", any()) } returns
            Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove last admin"))

        authenticatedClient().delete().uri("/api/v1/problem-sets/SHARE123/members/me").exchange().expectStatus().isBadRequest
    }

    // endregion

    // region GET /api/v1/problem-sets/{shareCode}/invitations

    @Test
    fun `getInvitations returns 200 with invitee names`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.loadForModerator("SHARE123", any()) } returns Mono.just(ps)
        every { problemSetMapper.toInvitedUserNames(ps) } returns Flux.just("invitee-1")

        authenticatedClient()
            .get()
            .uri("/api/v1/problem-sets/SHARE123/invitations")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<String>()
            .hasSize(1)
    }

    // endregion

    // region POST /api/v1/problem-sets/{shareCode}/invitations

    @Test
    fun `createInvitation returns 200 with updated DTO`() {
        val invitee = BackendFixtures.user(id = 2L, name = "invitee")
        val ps = BackendFixtures.problemSet()
        every { userService.findUserByName("invitee") } returns Mono.just(invitee)
        every { problemSetService.createInvitation("SHARE123", 2L, any()) } returns Mono.just(ps)
        every { problemSetMapper.toDto(ps, any()) } returns Mono.just(psDto())

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/invitations")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(CreateInvitationRequest("invitee"))
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `createInvitation returns 404 when invitee does not exist`() {
        every { userService.findUserByName("ghost") } returns Mono.empty()

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/invitations")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(CreateInvitationRequest("ghost"))
            .exchange()
            .expectStatus()
            .isNotFound
    }

    // endregion

    // region DELETE /api/v1/problem-sets/{shareCode}/invitations/{username}

    @Test
    fun `revokeInvitation returns 200 with updated DTO`() {
        val invitee = BackendFixtures.user(id = 2L, name = "invitee")
        val ps = BackendFixtures.problemSet()
        every { userService.findUserByName("invitee") } returns Mono.just(invitee)
        every { problemSetService.revokeInvitation("SHARE123", 2L, any()) } returns Mono.just(ps)
        every { problemSetMapper.toDto(ps, any()) } returns Mono.just(psDto())

        authenticatedClient()
            .delete()
            .uri("/api/v1/problem-sets/SHARE123/invitations/invitee")
            .exchange()
            .expectStatus()
            .isOk
    }

    // endregion

    // region POST /api/v1/problem-sets/{shareCode}/invitations/me/accept

    @Test
    fun `acceptInvitation returns 200 with updated DTO`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.acceptInvitation("SHARE123", any()) } returns Mono.just(ps)
        every { problemSetMapper.toDto(ps, any()) } returns Mono.just(psDto())

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/invitations/me/accept")
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `acceptInvitation returns 403 when caller has no invitation`() {
        every { problemSetService.acceptInvitation("SHARE123", any()) } returns
            Mono.error(ResponseStatusException(HttpStatus.FORBIDDEN, "No invitation"))

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/invitations/me/accept")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    // endregion

    // region POST /api/v1/problem-sets/{shareCode}/invitations/me/decline

    @Test
    fun `declineInvitation returns 204`() {
        val ps = BackendFixtures.problemSet()
        every { problemSetService.declineInvitation("SHARE123", any()) } returns Mono.just(ps)

        authenticatedClient()
            .post()
            .uri("/api/v1/problem-sets/SHARE123/invitations/me/decline")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    // endregion
}
