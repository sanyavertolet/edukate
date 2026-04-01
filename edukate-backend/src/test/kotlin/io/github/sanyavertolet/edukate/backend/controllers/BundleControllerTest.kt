@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.BundleDto
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata
import io.github.sanyavertolet.edukate.backend.dtos.ChangeBundleProblemsRequest
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole
import io.github.sanyavertolet.edukate.backend.services.BundleService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(BundleController::class)
@Import(NoopWebSecurityConfig::class)
class BundleControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var bundleService: BundleService
    @MockkBean private lateinit var userService: UserService
    @MockkBean private lateinit var notifier: Notifier

    private fun authenticatedClient(): WebTestClient =
        webTestClient.mutateWith(
            SecurityMockServerConfigurers.mockAuthentication(BackendFixtures.mockAuthentication(userId = "user-1"))
        )

    private fun bundleDto(shareCode: String = "SHARE123") =
        BundleDto("Test Bundle", "Description", listOf("admin-1"), false, emptyList(), shareCode)

    private fun bundleMetadata(shareCode: String = "SHARE123") =
        BundleMetadata("Test Bundle", "Description", listOf("admin-1"), shareCode, false, 1L)

    // region POST /api/v1/bundles

    @Test
    fun `createBundle returns 200 with bundle DTO`() {
        val bundle = BackendFixtures.bundle()
        every { bundleService.createBundle(any(), any()) } returns Mono.just(bundle)
        every { bundleService.prepareDto(bundle, any()) } returns Mono.just(bundleDto())

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(CreateBundleRequest("Test Bundle", "Description", false, listOf("1.0.0")))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.shareCode").isEqualTo("SHARE123")
            .jsonPath("$.name").isEqualTo("Test Bundle")
    }

    // endregion

    // region GET /api/v1/bundles/public

    @Test
    fun `getPublicBundles returns 200 with metadata list (no auth required)`() {
        val bundle = BackendFixtures.bundle(isPublic = true)
        every { bundleService.getPublicBundles(any()) } returns Flux.just(bundle)
        every { bundleService.prepareMetadata(bundle) } returns Mono.just(bundleMetadata())

        webTestClient
            .get()
            .uri("/api/v1/bundles/public")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(BundleMetadata::class.java)
            .hasSize(1)
    }

    // endregion

    // region GET /api/v1/bundles/owned

    @Test
    fun `getOwnedBundles returns 200 with owned bundle metadata`() {
        val bundle = BackendFixtures.bundle()
        every { bundleService.getOwnedBundles(any(), any()) } returns Flux.just(bundle)
        every { bundleService.prepareMetadata(bundle) } returns Mono.just(bundleMetadata())

        authenticatedClient()
            .get()
            .uri("/api/v1/bundles/owned")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(BundleMetadata::class.java)
            .hasSize(1)
    }

    // endregion

    // region GET /api/v1/bundles/{shareCode}

    @Test
    fun `getBundleByShareCode returns 200 when user is a member`() {
        // Bundle with "user-1" as a USER — passes isUserInBundle filter
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("user-1" to UserRole.USER))
        every { bundleService.findBundleByShareCode("SHARE123") } returns Mono.just(bundle)
        every { bundleService.prepareDto(bundle, any()) } returns Mono.just(bundleDto())

        authenticatedClient()
            .get()
            .uri("/api/v1/bundles/SHARE123")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.shareCode").isEqualTo("SHARE123")
    }

    @Test
    fun `getBundleByShareCode returns 403 when user is not a member`() {
        // Bundle with only "admin-1" — "user-1" is not a member, filter returns empty → 403
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN))
        every { bundleService.findBundleByShareCode("SHARE123") } returns Mono.just(bundle)

        authenticatedClient()
            .get()
            .uri("/api/v1/bundles/SHARE123")
            .exchange()
            .expectStatus().isForbidden
    }

    // endregion

    // region POST /api/v1/bundles/{shareCode}/join

    @Test
    fun `joinBundle returns 200 with bundle metadata`() {
        val bundle = BackendFixtures.bundle()
        every { bundleService.joinUser("SHARE123", "user-1") } returns Mono.just(bundle)
        every { bundleService.prepareMetadata(bundle) } returns Mono.just(bundleMetadata())

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/join")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.shareCode").isEqualTo("SHARE123")
    }

    // endregion

    // region GET /api/v1/bundles/joined

    @Test
    fun `getJoinedBundles returns 200 with joined bundle metadata`() {
        val bundle = BackendFixtures.bundle()
        every { bundleService.getJoinedBundles(any(), any()) } returns Flux.just(bundle)
        every { bundleService.prepareMetadata(bundle) } returns Mono.just(bundleMetadata())

        authenticatedClient()
            .get()
            .uri("/api/v1/bundles/joined")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(BundleMetadata::class.java)
            .hasSize(1)
    }

    // endregion

    // region POST /api/v1/bundles/{shareCode}/leave

    @Test
    fun `leaveBundle returns 200 with shareCode`() {
        val bundle = BackendFixtures.bundle()
        every { bundleService.removeUser("SHARE123", "user-1") } returns Mono.just(bundle)

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/leave")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `leaveBundle returns 400 when last admin`() {
        every { bundleService.removeUser("SHARE123", "user-1") } returns
            Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove last admin"))

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/leave")
            .exchange()
            .expectStatus().isBadRequest
    }

    // endregion

    // region POST /api/v1/bundles/{shareCode}/invite

    @Test
    fun `inviteToBundle returns 200 success message`() {
        val invitee = BackendFixtures.user(id = "invitee-1", name = "invitee")
        val bundle = BackendFixtures.bundle()
        every { userService.findUserByName("invitee") } returns Mono.just(invitee)
        every { bundleService.inviteUser("SHARE123", "user-1", "invitee-1") } returns Mono.just(bundle)
        every { notifier.notify(any()) } returns Mono.just("notification-id")

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/invite?inviteeName=invitee")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `inviteToBundle returns 403 when requester lacks invite permission`() {
        val invitee = BackendFixtures.user(id = "invitee-1", name = "invitee")
        every { userService.findUserByName("invitee") } returns Mono.just(invitee)
        every { bundleService.inviteUser("SHARE123", "user-1", "invitee-1") } returns
            Mono.error(ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions"))

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/invite?inviteeName=invitee")
            .exchange()
            .expectStatus().isForbidden
    }

    // endregion

    // region POST /api/v1/bundles/{shareCode}/expire-invite

    @Test
    fun `expireInvite returns 200 success message`() {
        val invitee = BackendFixtures.user(id = "invitee-1", name = "invitee")
        val bundle = BackendFixtures.bundle()
        every { userService.findUserByName("invitee") } returns Mono.just(invitee)
        every { bundleService.expireInvite("SHARE123", "user-1", "invitee-1") } returns Mono.just(bundle)

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/expire-invite?inviteeName=invitee")
            .exchange()
            .expectStatus().isOk
    }

    // endregion

    // region POST /api/v1/bundles/{shareCode}/reply-invite

    @Test
    fun `replyToInvite accept returns 200 success message`() {
        val bundle = BackendFixtures.bundle()
        every { bundleService.joinUser("SHARE123", "user-1") } returns Mono.just(bundle)

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/reply-invite?response=true")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `replyToInvite decline returns 200 success message`() {
        val bundle = BackendFixtures.bundle()
        every { bundleService.declineInvite("SHARE123", any()) } returns Mono.just(bundle)

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/reply-invite?response=false")
            .exchange()
            .expectStatus().isOk
    }

    // endregion

    // region GET /api/v1/bundles/{shareCode}/users

    @Test
    fun `getUserRoles returns 200 with user role list`() {
        every { bundleService.getBundleUsers("SHARE123", any()) } returns
            Flux.just(UserNameWithRole("admin-1", UserRole.ADMIN))

        authenticatedClient()
            .get()
            .uri("/api/v1/bundles/SHARE123/users")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(UserNameWithRole::class.java)
            .hasSize(1)
    }

    // endregion

    // region GET /api/v1/bundles/{shareCode}/invited-users

    @Test
    fun `getInvitedUsers returns 200 with invited user list`() {
        every { bundleService.getBundleInvitedUsers("SHARE123", any()) } returns Flux.just("invitee-1")

        authenticatedClient()
            .get()
            .uri("/api/v1/bundles/SHARE123/invited-users")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .hasSize(1)
    }

    // endregion

    // region POST /api/v1/bundles/{shareCode}/role

    @Test
    fun `changeUserRole returns 200 with updated role`() {
        val targetUser = BackendFixtures.user(id = "target-1", name = "target")
        every { userService.findUserByName("target") } returns Mono.just(targetUser)
        every { bundleService.changeUserRole("SHARE123", "target-1", UserRole.MODERATOR, any()) } returns
            Mono.just(UserRole.MODERATOR)

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/role?username=target&requestedRole=MODERATOR")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `changeUserRole returns 403 when requester lacks permission`() {
        val targetUser = BackendFixtures.user(id = "target-1", name = "target")
        every { userService.findUserByName("target") } returns Mono.just(targetUser)
        every { bundleService.changeUserRole("SHARE123", "target-1", UserRole.ADMIN, any()) } returns
            Mono.error(ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions"))

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/role?username=target&requestedRole=ADMIN")
            .exchange()
            .expectStatus().isForbidden
    }

    // endregion

    // region POST /api/v1/bundles/{shareCode}/visibility

    @Test
    fun `changeVisibility returns 200 with updated bundle DTO`() {
        val bundle = BackendFixtures.bundle(isPublic = true)
        every { bundleService.changeVisibility("SHARE123", true, any()) } returns Mono.just(bundle)
        every { bundleService.prepareDto(bundle, any()) } returns Mono.just(bundleDto())

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/visibility?isPublic=true")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.shareCode").isEqualTo("SHARE123")
    }

    // endregion

    // region POST /api/v1/bundles/{shareCode}/problems

    @Test
    fun `changeProblems returns 200 with updated bundle DTO`() {
        val bundle = BackendFixtures.bundle(problemIds = listOf("1.0.0", "2.0.0"))
        every { bundleService.changeProblems("SHARE123", listOf("1.0.0", "2.0.0"), any()) } returns Mono.just(bundle)
        every { bundleService.prepareDto(bundle, any()) } returns Mono.just(bundleDto())

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/problems")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ChangeBundleProblemsRequest(listOf("1.0.0", "2.0.0")))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.shareCode").isEqualTo("SHARE123")
    }

    @Test
    fun `changeProblems returns 400 when problem list is empty`() {
        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SHARE123/problems")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ChangeBundleProblemsRequest(emptyList()))
            .exchange()
            .expectStatus().isBadRequest
    }

    // endregion
}
