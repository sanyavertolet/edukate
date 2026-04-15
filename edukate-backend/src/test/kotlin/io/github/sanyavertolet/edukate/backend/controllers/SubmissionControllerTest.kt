@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.SubmissionDto
import io.github.sanyavertolet.edukate.backend.mappers.SubmissionMapper
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.backend.services.SubmissionService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.mockk.every
import java.time.Instant
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(SubmissionController::class)
@Import(NoopWebSecurityConfig::class)
class SubmissionControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var problemService: ProblemService
    @MockkBean private lateinit var userService: UserService
    @MockkBean private lateinit var submissionService: SubmissionService
    @MockkBean private lateinit var submissionMapper: SubmissionMapper
    @MockkBean private lateinit var fileManager: FileManager

    private fun authenticatedClient(): WebTestClient =
        webTestClient.mutateWith(
            SecurityMockServerConfigurers.mockAuthentication(BackendFixtures.mockAuthentication(userId = "user-1"))
        )

    private fun submissionDto(id: String = "sub-1", status: SubmissionStatus = SubmissionStatus.PENDING) =
        SubmissionDto(
            id = id,
            problemId = "1.0.0",
            userName = "testuser",
            status = status,
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            fileUrls = emptyList(),
        )

    // region GET /api/v1/submissions/by-id/{id}

    @Test
    fun `getSubmissionById returns 200 with submission DTO when owner matches`() {
        val submission = BackendFixtures.submission(id = "sub-1", userId = "user-1")
        val dto = submissionDto(id = "sub-1")

        every { submissionService.findById("sub-1") } returns Mono.just(submission)
        every { submissionMapper.toDto(submission) } returns Mono.just(dto)

        authenticatedClient()
            .get()
            .uri("/api/v1/submissions/by-id/sub-1")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("sub-1")
            .jsonPath("$.status")
            .isEqualTo("PENDING")
    }

    @Test
    fun `getSubmissionById returns 404 when submission does not exist`() {
        every { submissionService.findById("missing") } returns Mono.empty()

        authenticatedClient().get().uri("/api/v1/submissions/by-id/missing").exchange().expectStatus().isNotFound
    }

    // endregion

    // region GET /api/v1/submissions/my

    @Test
    fun `getMySubmissions returns 200 with list of DTOs`() {
        val dto1 = submissionDto(id = "sub-1")
        val dto2 = submissionDto(id = "sub-2")

        every { submissionService.findUserSubmissions(any(), isNull(), any()) } returns
            Flux.just(BackendFixtures.submission(id = "sub-1"), BackendFixtures.submission(id = "sub-2"))
        every { submissionMapper.toDto(any()) } returnsMany listOf(Mono.just(dto1), Mono.just(dto2))

        authenticatedClient()
            .get()
            .uri("/api/v1/submissions/my")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<SubmissionDto>()
            .hasSize(2)
    }

    @Test
    fun `getMySubmissions returns empty list when user has no submissions`() {
        every { submissionService.findUserSubmissions(any(), isNull(), any()) } returns Flux.empty()

        authenticatedClient()
            .get()
            .uri("/api/v1/submissions/my")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<SubmissionDto>()
            .hasSize(0)
    }

    @Test
    fun `getMySubmissions filters by problemId when provided`() {
        every { submissionService.findUserSubmissions(any(), eq("1.0.0"), any()) } returns Flux.empty()

        authenticatedClient().get().uri("/api/v1/submissions/my?problemId=1.0.0").exchange().expectStatus().isOk
    }

    @Test
    fun `getMySubmissions applies custom page and size`() {
        every { submissionService.findUserSubmissions(any(), isNull(), any()) } returns Flux.empty()

        authenticatedClient().get().uri("/api/v1/submissions/my?page=2&size=20").exchange().expectStatus().isOk
    }

    // endregion

}
