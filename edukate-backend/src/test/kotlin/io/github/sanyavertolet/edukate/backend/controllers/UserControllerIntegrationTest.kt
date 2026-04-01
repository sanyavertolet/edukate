package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.AbstractBackendIntegrationTest
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.repositories.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UserControllerIntegrationTest : AbstractBackendIntegrationTest() {

    @Autowired private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll().block()
        userRepository.save(BackendFixtures.user(id = null, name = "testuser")).block()
    }

    // region GET /api/v1/users/whoami

    @Test
    fun `whoami returns 200 with current user data`() {
        authenticatedClient()
            .get()
            .uri("/api/v1/users/whoami")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.name")
            .isEqualTo("testuser")
    }

    // endregion
}
