@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.services.ProblemSetService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(UserController::class)
@Import(NoopWebSecurityConfig::class)
class UserControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var userService: UserService
    @Suppress("unused") @MockkBean private lateinit var problemSetService: ProblemSetService

    private fun authenticatedClient(): WebTestClient =
        webTestClient.mutateWith(
            SecurityMockServerConfigurers.mockAuthentication(BackendFixtures.mockAuthentication(userId = 1L))
        )

    // region GET /api/v1/users/whoami

    @Test
    fun `whoami returns 200 with user data including email when user found`() {
        val user = BackendFixtures.user(id = 1L, name = "testuser", email = "testuser@example.com")
        every { userService.findUserByName("testuser") } returns Mono.just(user)

        authenticatedClient()
            .get()
            .uri("/api/v1/users/whoami")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.name")
            .isEqualTo("testuser")
            .jsonPath("$.email")
            .isEqualTo("testuser@example.com")
            .jsonPath("$.status")
            .isEqualTo("ACTIVE")
    }

    @Test
    fun `whoami returns empty when user not found by name`() {
        every { userService.findUserByName("testuser") } returns Mono.empty()

        authenticatedClient().get().uri("/api/v1/users/whoami").exchange().expectStatus().isOk.expectBody().isEmpty
    }

    // endregion

    // region GET /api/v1/users/by-prefix

    @Test
    fun `getUserNamesByPrefix excludes the authenticated user from results`() {
        val self = BackendFixtures.user(id = 1L, name = "testuser")
        every { userService.findUserByName("testuser") } returns Mono.just(self)
        every { userService.getUserNamesByPrefix("al", 5, setOf(1L)) } returns Flux.just("alice", "albert")

        authenticatedClient()
            .get()
            .uri { it.path("/api/v1/users/by-prefix").queryParam("prefix", "al").build() }
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$[0]")
            .isEqualTo("alice")
            .jsonPath("$[1]")
            .isEqualTo("albert")
    }

    @Test
    fun `getUserNamesByPrefix returns results without exclusion when not authenticated`() {
        every { userService.getUserNamesByPrefix("al", 5, emptySet()) } returns Flux.just("alice", "albert")

        webTestClient
            .get()
            .uri { it.path("/api/v1/users/by-prefix").queryParam("prefix", "al").build() }
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.length()")
            .isEqualTo(2)
    }

    // endregion
}
