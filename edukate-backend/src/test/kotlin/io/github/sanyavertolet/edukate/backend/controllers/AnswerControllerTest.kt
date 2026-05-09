@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.AnswerDto
import io.github.sanyavertolet.edukate.backend.mappers.AnswerMapper
import io.github.sanyavertolet.edukate.backend.services.AnswerService
import io.github.sanyavertolet.edukate.common.ContentLanguage
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

    @MockkBean private lateinit var answerMapper: AnswerMapper

    // region GET /api/v1/answers/{bookSlug}/{code}

    @Test
    fun `getAnswer returns 200 with answer when found`() {
        val answer = BackendFixtures.answer(id = 1L, problemId = 1L)
        val dto = AnswerDto("42", "No notes", emptyList(), ContentLanguage.RU)
        every { answerService.findByProblemKey("savchenko/P1") } returns Mono.just(answer)
        every { answerMapper.toDto(answer, "savchenko/P1") } returns Mono.just(dto)

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
            .jsonPath("$.language")
            .isEqualTo("RU")
    }

    @Test
    fun `getAnswer returns 404 when answer not found`() {
        every { answerService.findByProblemKey("savchenko/UNKNOWN") } returns Mono.empty()

        webTestClient.get().uri("/api/v1/answers/savchenko/UNKNOWN").exchange().expectStatus().isNotFound
    }

    // endregion
}
