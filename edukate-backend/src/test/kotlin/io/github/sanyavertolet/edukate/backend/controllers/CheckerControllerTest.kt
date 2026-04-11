@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.services.CheckResultService
import io.github.sanyavertolet.edukate.backend.services.CheckerSchedulerService
import io.github.sanyavertolet.edukate.backend.services.SubmissionService
import io.github.sanyavertolet.edukate.common.checks.CheckResultInfo
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(CheckerController::class)
@Import(NoopWebSecurityConfig::class)
class CheckerControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var checkResultService: CheckResultService
    @MockkBean private lateinit var checkerSchedulerService: CheckerSchedulerService
    @MockkBean private lateinit var submissionService: SubmissionService

    private fun authenticatedClient(role: UserRole = UserRole.USER): WebTestClient =
        webTestClient.mutateWith(
            SecurityMockServerConfigurers.mockAuthentication(
                BackendFixtures.mockAuthentication(userId = "user-1", roles = setOf(role))
            )
        )

    // region POST /api/v1/checker/ai

    @Test
    fun `aiCheck returns 202 when submission found and check scheduled`() {
        val submission = BackendFixtures.submission(id = "sub-1", userId = "user-1")
        every { submissionService.getSubmissionIfOwns("sub-1", "user-1") } returns Mono.just(submission)
        every { checkerSchedulerService.scheduleCheck(submission) } returns Mono.empty()

        // currently for moderators only
        authenticatedClient(UserRole.MODERATOR)
            .post()
            .uri("/api/v1/checker/ai?id=sub-1")
            .exchange()
            .expectStatus()
            .isAccepted
    }

    @Test
    fun `aiCheck returns 404 when submission not found`() {
        every { submissionService.getSubmissionIfOwns("missing", "user-1") } returns
            Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND))

        authenticatedClient(UserRole.MODERATOR)
            .post()
            .uri("/api/v1/checker/ai?id=missing")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    // endregion

    // region POST /api/v1/checker/self

    @Test
    fun `selfCheck returns 202 when submission found`() {
        val submission = BackendFixtures.submission(id = "sub-1", userId = "user-1")
        val checkResult = BackendFixtures.checkResult(submissionId = "sub-1")
        every { submissionService.getSubmissionIfOwns("sub-1", "user-1") } returns Mono.just(submission)
        every { checkResultService.saveAndUpdateSubmission(any()) } returns Mono.just(checkResult to submission)

        authenticatedClient().post().uri("/api/v1/checker/self?id=sub-1").exchange().expectStatus().isAccepted
    }

    // endregion

    // region POST /api/v1/checker/supervisor

    @Test
    fun `supervisorCheck returns 501 not implemented`() {
        authenticatedClient()
            .post()
            .uri("/api/v1/checker/supervisor?id=sub-1")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.NOT_IMPLEMENTED)
    }

    // endregion

    // region GET /api/v1/checker/by-id/{id}

    @Test
    fun `getCheckResultById returns 200 with DTO when found and user owns submission`() {
        val checkResult = BackendFixtures.checkResult(id = "cr-1", submissionId = "sub-1")
        val submission = BackendFixtures.submission(id = "sub-1", userId = "user-1")

        every { checkResultService.findById("cr-1") } returns Mono.just(checkResult)
        every { submissionService.getSubmissionIfOwns("sub-1", "user-1") } returns Mono.just(submission)

        authenticatedClient()
            .get()
            .uri("/api/v1/checker/by-id/cr-1")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.status")
            .isEqualTo("SUCCESS")
    }

    @Test
    fun `getCheckResultById returns 404 when check result not found`() {
        every { checkResultService.findById("missing") } returns Mono.empty()

        authenticatedClient().get().uri("/api/v1/checker/by-id/missing").exchange().expectStatus().isNotFound
    }

    // endregion

    // region GET /api/v1/checker/submissions/{submissionId}

    @Test
    fun `getCheckResultsBySubmissionId returns list for owned submission`() {
        val submission = BackendFixtures.submission(id = "sub-1", userId = "user-1")
        val checkResult = BackendFixtures.checkResult(id = "cr-1", submissionId = "sub-1")
        every { submissionService.getSubmissionIfOwns("sub-1", "user-1") } returns Mono.just(submission)
        every { checkResultService.findAllBySubmissionId("sub-1") } returns Flux.just(checkResult)

        authenticatedClient()
            .get()
            .uri("/api/v1/checker/submissions/sub-1")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<CheckResultInfo>()
            .hasSize(1)
    }

    // endregion
}
