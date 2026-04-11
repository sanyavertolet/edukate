package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.AbstractBackendIntegrationTest
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.common.SubmissionStatus
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
        mongoTemplate.dropCollection("problem_status").block()
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
            .expectStatus()
            .isOk
            .expectBodyList<ProblemMetadata>()
            .hasSize(3)
    }

    @Test
    fun `getProblems respects page size`() {
        webTestClient
            .get()
            .uri("/api/v1/problems?size=2")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemMetadata>()
            .hasSize(2)
    }

    @Test
    fun `getProblems with prefix returns only matching problems`() {
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
    fun `getProblems with status SOLVED returns 401 when unauthenticated`() {
        webTestClient.get().uri("/api/v1/problems?prefix=1.&status=SOLVED").exchange().expectStatus().isUnauthorized
    }

    @Test
    fun `getProblems with status NOT_SOLVED returns all problems when unauthenticated`() {
        webTestClient
            .get()
            .uri("/api/v1/problems?status=NOT_SOLVED")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemMetadata>()
            .hasSize(3)
    }

    @Test
    fun `getProblems with status SOLVED returns only solved problems when authenticated`() {
        mongoTemplate
            .save(
                BackendFixtures.userProblemStatus(
                    userId = "user-1",
                    problemId = "1.0.0",
                    bestStatus = SubmissionStatus.SUCCESS,
                ),
                "problem_status",
            )
            .block()
        mongoTemplate
            .save(
                BackendFixtures.userProblemStatus(
                    userId = "user-1",
                    problemId = "1.1.0",
                    bestStatus = SubmissionStatus.FAILED,
                ),
                "problem_status",
            )
            .block()

        authenticatedClient()
            .get()
            .uri("/api/v1/problems?status=SOLVED")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemMetadata>()
            .hasSize(1)
            .contains(
                ProblemMetadata(
                    "1.0.0",
                    false,
                    emptyList(),
                    io.github.sanyavertolet.edukate.backend.entities.Problem.Status.SOLVED,
                )
            )
    }

    @Test
    fun `getProblems with status NOT_SOLVED returns unsolved problems when authenticated`() {
        mongoTemplate
            .save(
                BackendFixtures.userProblemStatus(
                    userId = "user-1",
                    problemId = "1.0.0",
                    bestStatus = SubmissionStatus.SUCCESS,
                ),
                "problem_status",
            )
            .block()

        authenticatedClient()
            .get()
            .uri("/api/v1/problems?status=NOT_SOLVED")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemMetadata>()
            .hasSize(2)
    }

    @Test
    fun `getProblems with prefix and status SOLVED returns only matching solved problems when authenticated`() {
        mongoTemplate
            .save(
                BackendFixtures.userProblemStatus(
                    userId = "user-1",
                    problemId = "1.0.0",
                    bestStatus = SubmissionStatus.SUCCESS,
                ),
                "problem_status",
            )
            .block()
        mongoTemplate
            .save(
                BackendFixtures.userProblemStatus(
                    userId = "user-1",
                    problemId = "2.0.0",
                    bestStatus = SubmissionStatus.SUCCESS,
                ),
                "problem_status",
            )
            .block()

        authenticatedClient()
            .get()
            .uri("/api/v1/problems?prefix=1.&status=SOLVED")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemMetadata>()
            .hasSize(1)
            .contains(
                ProblemMetadata(
                    "1.0.0",
                    false,
                    emptyList(),
                    io.github.sanyavertolet.edukate.backend.entities.Problem.Status.SOLVED,
                )
            )
    }

    // endregion

    // region GET /api/v1/problems/count

    @Test
    fun `count returns total number of problems`() {
        webTestClient.get().uri("/api/v1/problems/count").exchange().expectStatus().isOk.expectBody<Long>().isEqualTo(3L)
    }

    @Test
    fun `count with prefix returns filtered count`() {
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
    fun `count with status SOLVED returns filtered count when authenticated`() {
        mongoTemplate
            .save(
                BackendFixtures.userProblemStatus(
                    userId = "user-1",
                    problemId = "1.0.0",
                    bestStatus = SubmissionStatus.SUCCESS,
                ),
                "problem_status",
            )
            .block()

        authenticatedClient()
            .get()
            .uri("/api/v1/problems/count?status=SOLVED")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<Long>()
            .isEqualTo(1L)
    }

    @Test
    fun `count with status SOLVED returns 401 when unauthenticated`() {
        webTestClient.get().uri("/api/v1/problems/count?status=SOLVED").exchange().expectStatus().isUnauthorized
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
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$[0]")
            .isEqualTo("2.0.0")
    }

    // endregion

    // region GET /api/v1/problems/{id}

    @Test
    fun `getProblem returns 200 with problem dto when found`() {
        webTestClient
            .get()
            .uri("/api/v1/problems/1.0.0")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("1.0.0")
    }

    @Test
    fun `getProblem returns 404 when problem not found`() {
        webTestClient.get().uri("/api/v1/problems/9.9.9").exchange().expectStatus().isNotFound
    }

    // endregion

    // region GET /api/v1/problems/random

    @Test
    fun `getRandomProblem returns 200 with a problem id`() {
        webTestClient.get().uri("/api/v1/problems/random").exchange().expectStatus().isOk.expectBody<String>().value { id ->
            assert(id in listOf("1.0.0", "1.1.0", "2.0.0"))
        }
    }

    // endregion
}
