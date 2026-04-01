@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers.internal

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.github.sanyavertolet.edukate.common.users.UserCredentials
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.users.UserStatus
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest(UserInternalController::class)
@Import(NoopWebSecurityConfig::class)
class UserInternalControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var userService: UserService

    private fun credentials(id: String? = "user-1") =
        UserCredentials(id, "testuser", "password", "test@example.com", setOf(UserRole.USER), UserStatus.ACTIVE)

    // region POST /internal/users

    @Test
    fun `saveUser returns 200 with saved user credentials`() {
        val user = BackendFixtures.user()
        every { userService.saveUser(any()) } returns Mono.just(user)

        webTestClient
            .post()
            .uri("/internal/users")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(credentials(id = null))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.username").isEqualTo("testuser")
    }

    // endregion

    // region GET /internal/users/by-name/{name}

    @Test
    fun `getUserByName returns 200 with user credentials when found`() {
        val user = BackendFixtures.user()
        every { userService.findUserByName("testuser") } returns Mono.just(user)

        webTestClient
            .get()
            .uri("/internal/users/by-name/testuser")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.username").isEqualTo("testuser")
    }

    @Test
    fun `getUserByName returns 200 with empty body when user not found`() {
        every { userService.findUserByName("unknown") } returns Mono.empty()

        webTestClient
            .get()
            .uri("/internal/users/by-name/unknown")
            .exchange()
            .expectStatus().isOk
            .expectBody().isEmpty
    }

    // endregion

    // region GET /internal/users/by-id/{id}

    @Test
    fun `getUserById returns 200 with user credentials when found`() {
        val user = BackendFixtures.user()
        every { userService.findUserById("user-1") } returns Mono.just(user)

        webTestClient
            .get()
            .uri("/internal/users/by-id/user-1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("user-1")
    }

    @Test
    fun `getUserById returns 200 with empty body when user not found`() {
        every { userService.findUserById("nonexistent") } returns Mono.empty()

        webTestClient
            .get()
            .uri("/internal/users/by-id/nonexistent")
            .exchange()
            .expectStatus().isOk
            .expectBody().isEmpty
    }

    // endregion

    // region DELETE /internal/users/by-id/{id}

    @Test
    fun `deleteUserById returns 200 with deleted user id`() {
        every { userService.deleteUserById("user-1") } returns Mono.empty()

        webTestClient
            .delete()
            .uri("/internal/users/by-id/user-1")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("user-1")
    }

    // endregion
}
