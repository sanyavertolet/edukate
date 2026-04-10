package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.AbstractBackendIntegrationTest
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata
import io.github.sanyavertolet.edukate.backend.dtos.ChangeBundleProblemsRequest
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest
import io.github.sanyavertolet.edukate.backend.repositories.BundleRepository
import io.github.sanyavertolet.edukate.backend.repositories.UserRepository
import io.github.sanyavertolet.edukate.common.users.UserRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.expectBodyList

class BundleControllerIntegrationTest : AbstractBackendIntegrationTest() {

    @Autowired private lateinit var bundleRepository: BundleRepository
    @Autowired private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        bundleRepository.deleteAll().block()
        userRepository.deleteAll().block()
        // testuser is the admin of the default bundle (user-1 maps to UserRole.ADMIN)
        userRepository.save(BackendFixtures.user(id = "user-1", name = "testuser")).block()
    }

    // region POST /api/v1/bundles

    @Test
    fun `createBundle returns 200 with bundle dto`() {
        val request = CreateBundleRequest("My Bundle", "Desc", false, listOf("problem-1"))
        authenticatedClient()
            .post()
            .uri("/api/v1/bundles")
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.name")
            .isEqualTo("My Bundle")
    }

    @Test
    fun `createBundle returns 401 when unauthenticated`() {
        val request = CreateBundleRequest("My Bundle", "Desc", false, listOf("problem-1"))
        webTestClient.post().uri("/api/v1/bundles").bodyValue(request).exchange().expectStatus().isUnauthorized
    }

    // endregion

    // region GET /api/v1/bundles/owned

    @Test
    fun `getOwnedBundles returns bundles where user is admin`() {
        bundleRepository
            .save(BackendFixtures.bundle("own", userIdRoleMap = mapOf("user-1" to UserRole.ADMIN), shareCode = "SC-OWN"))
            .block()
        bundleRepository
            .save(
                BackendFixtures.bundle(
                    "other",
                    userIdRoleMap = mapOf("other-user" to UserRole.ADMIN),
                    shareCode = "SC-OTHER",
                )
            )
            .block()

        authenticatedClient()
            .get()
            .uri("/api/v1/bundles/owned")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<BundleMetadata>()
            .hasSize(1)
    }

    // endregion

    // region GET /api/v1/bundles/public

    @Test
    fun `getPublicBundles returns public bundles without auth`() {
        bundleRepository.save(BackendFixtures.bundle("pub", isPublic = true, shareCode = "SC-PUB")).block()
        bundleRepository.save(BackendFixtures.bundle("priv", isPublic = false, shareCode = "SC-PRIV")).block()

        webTestClient
            .get()
            .uri("/api/v1/bundles/public")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<BundleMetadata>()
            .hasSize(1)
    }

    // endregion

    // region POST /api/v1/bundles/{shareCode}/join

    @Test
    fun `joinBundle returns 200 for public bundle`() {
        bundleRepository
            .save(
                BackendFixtures.bundle(
                    isPublic = true,
                    shareCode = "SC-JOIN",
                    userIdRoleMap = mapOf("other-user" to UserRole.ADMIN),
                )
            )
            .block()

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SC-JOIN/join")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.shareCode")
            .isEqualTo("SC-JOIN")
    }

    @Test
    fun `joinBundle returns 400 when already a member`() {
        bundleRepository
            .save(
                BackendFixtures.bundle(
                    isPublic = true,
                    shareCode = "SC-JOINED",
                    userIdRoleMap = mapOf("user-1" to UserRole.ADMIN),
                )
            )
            .block()

        authenticatedClient().post().uri("/api/v1/bundles/SC-JOINED/join").exchange().expectStatus().isBadRequest
    }

    // endregion

    // region POST /api/v1/bundles/{shareCode}/leave

    @Test
    fun `leaveBundle returns 200 when user is not the last admin`() {
        bundleRepository
            .save(
                BackendFixtures.bundle(
                    shareCode = "SC-LEAVE",
                    userIdRoleMap = mapOf("user-1" to UserRole.USER, "other-admin" to UserRole.ADMIN),
                )
            )
            .block()

        authenticatedClient().post().uri("/api/v1/bundles/SC-LEAVE/leave").exchange().expectStatus().isOk
    }

    @Test
    fun `leaveBundle returns 400 when user is the last admin`() {
        bundleRepository
            .save(BackendFixtures.bundle(shareCode = "SC-LAST-ADMIN", userIdRoleMap = mapOf("user-1" to UserRole.ADMIN)))
            .block()

        authenticatedClient().post().uri("/api/v1/bundles/SC-LAST-ADMIN/leave").exchange().expectStatus().isBadRequest
    }

    // endregion

    // region POST /api/v1/bundles/{shareCode}/visibility

    @Test
    fun `changeVisibility returns 200 when user is admin`() {
        bundleRepository
            .save(
                BackendFixtures.bundle(
                    isPublic = false,
                    shareCode = "SC-VIS",
                    userIdRoleMap = mapOf("user-1" to UserRole.ADMIN),
                )
            )
            .block()

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SC-VIS/visibility?isPublic=true")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.shareCode")
            .isEqualTo("SC-VIS")
    }

    @Test
    fun `changeVisibility returns 403 when user is not admin`() {
        bundleRepository
            .save(
                BackendFixtures.bundle(
                    shareCode = "SC-VIS-NOAUTH",
                    userIdRoleMap = mapOf("user-1" to UserRole.USER, "admin" to UserRole.ADMIN),
                )
            )
            .block()

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SC-VIS-NOAUTH/visibility?isPublic=true")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    // endregion

    // region POST /api/v1/bundles/{shareCode}/problems

    @Test
    fun `changeProblems returns 200 when user is admin`() {
        bundleRepository
            .save(BackendFixtures.bundle(shareCode = "SC-PROB", userIdRoleMap = mapOf("user-1" to UserRole.ADMIN)))
            .block()

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SC-PROB/problems")
            .bodyValue(ChangeBundleProblemsRequest(listOf("1.0.0", "2.0.0")))
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `changeProblems returns 400 when problem list is empty`() {
        bundleRepository
            .save(BackendFixtures.bundle(shareCode = "SC-EMPTY-PROB", userIdRoleMap = mapOf("user-1" to UserRole.ADMIN)))
            .block()

        authenticatedClient()
            .post()
            .uri("/api/v1/bundles/SC-EMPTY-PROB/problems")
            .bodyValue(ChangeBundleProblemsRequest(emptyList()))
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    // endregion
}
