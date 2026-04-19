@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.dtos.AnswerDto
import io.github.sanyavertolet.edukate.backend.services.AnswerService
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest(AnswerController::class)
@Import(NoopWebSecurityConfig::class)
class AnswerControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var answerService: AnswerService

    // region GET /api/v1/answers/{bookSlug}/{code}

    @Test
    fun `getAnswer returns 200 with answer when found`() {
        val answer = AnswerDto("42", "No notes", emptyList())
        every { answerService.findByProblemKey("savchenko/P1") } returns Mono.just(answer)

        webTestClient
            .get()
            .uri("/api/v1/answers/savchenko/P1")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.text")
            .isEqualTo("42")
    }

    @Test
    fun `getAnswer returns 404 when answer not found`() {
        every { answerService.findByProblemKey("savchenko/UNKNOWN") } returns Mono.empty()

        webTestClient.get().uri("/api/v1/answers/savchenko/UNKNOWN").exchange().expectStatus().isNotFound
    }

    // endregion
}
