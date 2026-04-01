package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.AbstractBackendIntegrationTest
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList
import reactor.core.publisher.Mono

class ProblemControllerIntegrationTest : AbstractBackendIntegrationTest() {

    @Autowired private lateinit var problemRepository: ProblemRepository
    @Autowired private lateinit var mongoTemplate: ReactiveMongoTemplate

    @BeforeEach
    fun setUp() {
        problemRepository.deleteAll().block()
        mongoTemplate.dropCollection("user_problem_statuses").block()
        problemRepository.save(BackendFixtures.problem(id = "1.0.0")).block()
        problemRepository.save(BackendFixtures.problem(id = "1.1.0")).block()
        problemRepository.save(BackendFixtures.problem(id = "2.0.0")).block()
        @Suppress("ReactiveStreamsUnusedPublisher")
        every { fileKeyStorage.generatePresignedUrl(any()) } returns Mono.just("http://presigned-url")
    }

    // region GET /api/v1/problems

    @Test
    fun `getProblems returns 200 with paginated list`() {
        webTestClient
            .get()
            .uri("/api/v1/problems")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<ProblemMetadata>()
            .hasSize(3)
    }

    @Test
    fun `getProblems respects page size`() {
        webTestClient
            .get()
            .uri("/api/v1/problems?size=2")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<ProblemMetadata>()
            .hasSize(2)
    }

    // endregion

    // region GET /api/v1/problems/count

    @Test
    fun `count returns total number of problems`() {
        webTestClient
            .get()
            .uri("/api/v1/problems/count")
            .exchange()
            .expectStatus().isOk
            .expectBody<Long>()
            .isEqualTo(3L)
    }

    // endregion

    // region GET /api/v1/problems/by-prefix

    @Test
    fun `getProblemsByPrefix returns matching problem ids`() {
        // prefix "2." matches only "2.0.0" — verifies filtering excludes non-matching problems
        webTestClient
            .get()
            .uri("/api/v1/problems/by-prefix?prefix=2.&limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0]").isEqualTo("2.0.0")
    }

    // endregion

    // region GET /api/v1/problems/{id}

    @Test
    fun `getProblem returns 200 with problem dto when found`() {
        webTestClient
            .get()
            .uri("/api/v1/problems/1.0.0")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("1.0.0")
    }

    @Test
    fun `getProblem returns 404 when problem not found`() {
        webTestClient
            .get()
            .uri("/api/v1/problems/9.9.9")
            .exchange()
            .expectStatus().isNotFound
    }

    // endregion

    // region GET /api/v1/problems/random

    @Test
    fun `getRandomProblem returns 200 with a problem id`() {
        webTestClient
            .get()
            .uri("/api/v1/problems/random")
            .exchange()
            .expectStatus().isOk
            .expectBody<String>()
            .value { id -> assert(id in listOf("1.0.0", "1.1.0", "2.0.0")) }
    }

    // endregion
}
