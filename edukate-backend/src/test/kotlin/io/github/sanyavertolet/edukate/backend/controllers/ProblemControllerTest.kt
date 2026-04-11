@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.filters.ProblemFilter
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(ProblemController::class)
@Import(NoopWebSecurityConfig::class)
class ProblemControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var problemService: ProblemService

    private fun metadata(id: String = "1.0.0") = ProblemMetadata(id, false, emptyList(), Problem.Status.NOT_SOLVED)

    private fun dto(@Suppress("SameParameterValue") id: String = "1.0.0") =
        ProblemDto(id, false, emptyList(), "Problem text", emptyList(), emptyList(), Problem.Status.NOT_SOLVED, false)

    // region GET /api/v1/problems

    @Test
    fun `getProblemList returns 200 with metadata list`() {
        every { problemService.getFilteredProblems(eq(ProblemFilter()), isNull(), any()) } returns
            Flux.just(BackendFixtures.problem("1.0.0"), BackendFixtures.problem("1.1.0"))
        every { problemService.prepareMetadata(any(), isNull()) } returnsMany
            listOf(Mono.just(metadata("1.0.0")), Mono.just(metadata("1.1.0")))

        webTestClient
            .get()
            .uri("/api/v1/problems")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBodyList<ProblemMetadata>()
            .hasSize(2)
    }

    @Test
    fun `getProblemList with prefix filter passes prefix to service`() {
        every { problemService.getFilteredProblems(eq(ProblemFilter(prefix = "1.")), isNull(), any()) } returns
            Flux.just(BackendFixtures.problem("1.0.0"), BackendFixtures.problem("1.1.0"))
        every { problemService.prepareMetadata(any(), isNull()) } returnsMany
            listOf(Mono.just(metadata("1.0.0")), Mono.just(metadata("1.1.0")))

        webTestClient
            .get()
            .uri("/api/v1/problems?prefix=1.")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemMetadata>()
            .hasSize(2)
    }

    @Test
    fun `getProblemList with status filter passes status to service`() {
        every {
            problemService.getFilteredProblems(eq(ProblemFilter(status = Problem.Status.SOLVED)), isNull(), any())
        } returns Flux.just(BackendFixtures.problem("1.0.0"))
        every { problemService.prepareMetadata(any(), isNull()) } returns Mono.just(metadata("1.0.0"))

        webTestClient
            .get()
            .uri("/api/v1/problems?status=SOLVED")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemMetadata>()
            .hasSize(1)
    }

    // endregion

    // region GET /api/v1/problems/count

    @Test
    fun `count returns problem count`() {
        every { problemService.countFilteredProblems(eq(ProblemFilter()), isNull()) } returns Mono.just(7L)

        webTestClient.get().uri("/api/v1/problems/count").exchange().expectStatus().isOk.expectBody<Long>().isEqualTo(7L)
    }

    @Test
    fun `count with prefix returns filtered count`() {
        every { problemService.countFilteredProblems(eq(ProblemFilter(prefix = "1.")), isNull()) } returns Mono.just(2L)

        webTestClient
            .get()
            .uri("/api/v1/problems/count?prefix=1.")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<Long>()
            .isEqualTo(2L)
    }

    @Test
    fun `count with status returns filtered count`() {
        every { problemService.countFilteredProblems(eq(ProblemFilter(status = Problem.Status.SOLVED)), isNull()) } returns
            Mono.just(1L)

        webTestClient
            .get()
            .uri("/api/v1/problems/count?status=SOLVED")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<Long>()
            .isEqualTo(1L)
    }

    // endregion

    // region GET /api/v1/problems/by-prefix

    @Test
    fun `getProblemIdsByPrefix returns matching ids`() {
        every { problemService.getProblemIdsByPrefix("1.", 5) } returns Flux.just("1.0.0", "1.1.0")

        webTestClient
            .get()
            .uri("/api/v1/problems/by-prefix?prefix=1.")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$[0]")
            .isEqualTo("1.0.0")
            .jsonPath("$[1]")
            .isEqualTo("1.1.0")
    }

    // endregion

    // region GET /api/v1/problems/{id}

    @Test
    fun `getProblem returns 200 when problem found`() {
        val problem = BackendFixtures.problem("1.0.0")
        every { problemService.findProblemById("1.0.0") } returns Mono.just(problem)
        every { problemService.prepareDto(problem, isNull()) } returns Mono.just(dto("1.0.0"))

        webTestClient
            .get()
            .uri("/api/v1/problems/1.0.0")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("1.0.0")
    }

    @Test
    fun `getProblem returns 404 when problem not found`() {
        every { problemService.findProblemById("missing") } returns Mono.empty()

        webTestClient.get().uri("/api/v1/problems/missing").exchange().expectStatus().isNotFound
    }

    // endregion

    // region GET /api/v1/problems/random

    @Test
    fun `getRandomUnsolvedProblemId returns problem id`() {
        every { problemService.getRandomUnsolvedProblemId(isNull()) } returns Mono.just("2.3.4")

        webTestClient
            .get()
            .uri("/api/v1/problems/random")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<String>()
            .isEqualTo("2.3.4")
    }

    // endregion
}
