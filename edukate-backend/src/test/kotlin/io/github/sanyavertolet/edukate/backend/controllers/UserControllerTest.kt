@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest(UserController::class)
@Import(NoopWebSecurityConfig::class)
class UserControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var userService: UserService

    private fun authenticatedClient(): WebTestClient =
        webTestClient.mutateWith(
            SecurityMockServerConfigurers.mockAuthentication(BackendFixtures.mockAuthentication(userId = "user-1"))
        )

    // region GET /api/v1/users/whoami

    @Test
    fun `whoami returns 200 with user data when user found`() {
        val user = BackendFixtures.user(id = "user-1", name = "testuser")
        every { userService.findUserByName("testuser") } returns Mono.just(user)

        authenticatedClient()
            .get()
            .uri("/api/v1/users/whoami")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.name").isEqualTo("testuser")
            .jsonPath("$.status").isEqualTo("ACTIVE")
    }

    @Test
    fun `whoami returns empty when user not found by name`() {
        every { userService.findUserByName("testuser") } returns Mono.empty()

        authenticatedClient()
            .get()
            .uri("/api/v1/users/whoami")
            .exchange()
            .expectStatus().isOk
            .expectBody().isEmpty
    }

    // endregion
}
