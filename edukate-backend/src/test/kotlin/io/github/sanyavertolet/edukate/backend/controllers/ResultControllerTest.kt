@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.dtos.Result
import io.github.sanyavertolet.edukate.backend.services.ResultService
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest(ResultController::class)
@Import(NoopWebSecurityConfig::class)
class ResultControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var resultService: ResultService

    // region GET /api/v1/results/{id}

    @Test
    fun `getResultById returns 200 with result when found`() {
        val result = Result("1.0.0", "42", "No notes", Result.ResultType.NUMERIC, emptyList())
        every { resultService.findResultById("1.0.0") } returns Mono.just(result)

        webTestClient
            .get()
            .uri("/api/v1/results/1.0.0")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").isEqualTo("1.0.0")
            .jsonPath("$.text").isEqualTo("42")
    }

    @Test
    fun `getResultById returns 404 when result not found`() {
        every { resultService.findResultById("missing") } returns Mono.empty()

        webTestClient.get().uri("/api/v1/results/missing").exchange().expectStatus().isNotFound
    }

    // endregion
}
