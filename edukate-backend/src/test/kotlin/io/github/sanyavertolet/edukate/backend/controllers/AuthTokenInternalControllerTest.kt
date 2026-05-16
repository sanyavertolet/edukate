@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.controllers.internal.AuthTokenInternalController
import io.github.sanyavertolet.edukate.backend.services.AuthTokenService
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.mockk.every
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@WebFluxTest(AuthTokenInternalController::class)
@Import(NoopWebSecurityConfig::class)
class AuthTokenInternalControllerTest {
    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var authTokenService: AuthTokenService

    // region POST /internal/tokens/verification

    @Test
    fun `issueVerificationToken returns 200 on success`() {
        every { authTokenService.issueVerificationToken(1L, "alice@example.com") } returns Mono.empty()

        webTestClient
            .post()
            .uri("/internal/tokens/verification")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("userId" to "1", "email" to "alice@example.com"))
            .exchange()
            .expectStatus()
            .isOk
    }

    // endregion

    // region POST /internal/tokens/password-reset

    @Test
    fun `issuePasswordResetToken returns 200 on success`() {
        every { authTokenService.issuePasswordResetToken("alice@example.com") } returns Mono.empty()

        webTestClient
            .post()
            .uri("/internal/tokens/password-reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("email" to "alice@example.com"))
            .exchange()
            .expectStatus()
            .isOk
    }

    // endregion

    // region POST /internal/tokens/consume/verification

    @Test
    fun `consumeVerificationToken returns 200 when token valid`() {
        val token = UUID.randomUUID()
        every { authTokenService.consumeVerificationToken(token) } returns Mono.empty()

        webTestClient
            .post()
            .uri("/internal/tokens/consume/verification")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("token" to token.toString()))
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `consumeVerificationToken returns 404 when token not found`() {
        val token = UUID.randomUUID()
        every { authTokenService.consumeVerificationToken(token) } returns
            Mono.error(ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))

        webTestClient
            .post()
            .uri("/internal/tokens/consume/verification")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("token" to token.toString()))
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `consumeVerificationToken returns 410 when token expired`() {
        val token = UUID.randomUUID()
        every { authTokenService.consumeVerificationToken(token) } returns
            Mono.error(ResponseStatusException(org.springframework.http.HttpStatus.GONE))

        webTestClient
            .post()
            .uri("/internal/tokens/consume/verification")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("token" to token.toString()))
            .exchange()
            .expectStatus()
            .isEqualTo(410)
    }

    // endregion

    // region POST /internal/tokens/consume/reset

    @Test
    fun `consumeResetToken returns 200 when token valid`() {
        val token = UUID.randomUUID()
        every { authTokenService.consumeResetToken(token, "encoded") } returns Mono.empty()

        webTestClient
            .post()
            .uri("/internal/tokens/consume/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("token" to token.toString(), "encodedPassword" to "encoded"))
            .exchange()
            .expectStatus()
            .isOk
    }

    // endregion
}
