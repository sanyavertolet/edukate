@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.SupervisorTicketDto
import io.github.sanyavertolet.edukate.backend.entities.ProblemSet
import io.github.sanyavertolet.edukate.backend.mappers.SupervisorTicketMapper
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.SupervisorTicketRepository
import io.github.sanyavertolet.edukate.backend.services.CheckResultService
import io.github.sanyavertolet.edukate.backend.services.CheckerSchedulerService
import io.github.sanyavertolet.edukate.backend.services.ProblemSetService
import io.github.sanyavertolet.edukate.backend.services.SubmissionService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.checks.CheckResultInfo
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.github.sanyavertolet.edukate.common.checks.SupervisorTicketStatus
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
    @MockkBean private lateinit var supervisorTicketRepository: SupervisorTicketRepository
    @MockkBean private lateinit var supervisorTicketMapper: SupervisorTicketMapper
    @MockkBean private lateinit var problemSetService: ProblemSetService
    @MockkBean private lateinit var userService: UserService
    @MockkBean private lateinit var problemRepository: ProblemRepository
    @MockkBean private lateinit var notifier: Notifier

    private fun authenticatedClient(userId: Long = 1L, role: UserRole = UserRole.USER): WebTestClient =
        webTestClient.mutateWith(
            SecurityMockServerConfigurers.mockAuthentication(
                BackendFixtures.mockAuthentication(userId = userId, roles = setOf(role))
            )
        )

    // region POST /api/v1/checker/ai

    @Test
    fun `aiCheck returns 202 when submission found and check scheduled`() {
        val submission = BackendFixtures.submission(id = 1L, userId = 1L)
        every { submissionService.getSubmissionIfOwns(1L, 1L) } returns Mono.just(submission)
        every { checkerSchedulerService.scheduleCheck(submission) } returns Mono.empty()

        authenticatedClient(role = UserRole.MODERATOR)
            .post()
            .uri("/api/v1/checker/ai?id=1")
            .exchange()
            .expectStatus()
            .isAccepted
    }

    @Test
    fun `aiCheck returns 404 when submission not found`() {
        every { submissionService.getSubmissionIfOwns(999L, 1L) } returns
            Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND))

        authenticatedClient(role = UserRole.MODERATOR)
            .post()
            .uri("/api/v1/checker/ai?id=999")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    // endregion

    // region POST /api/v1/checker/self

    @Test
    fun `selfCheck returns 202 when submission found`() {
        val submission = BackendFixtures.submission(id = 1L, userId = 1L)
        val checkResult = BackendFixtures.checkResult(submissionId = 1L)
        every { submissionService.getSubmissionIfOwns(1L, 1L) } returns Mono.just(submission)
        every { checkResultService.saveCheckResult(any()) } returns Mono.just(checkResult)

        authenticatedClient().post().uri("/api/v1/checker/self?id=1").exchange().expectStatus().isAccepted
    }

    // endregion

    // region POST /api/v1/checker/supervisor

    @Test
    fun `supervisorCheck returns 202 when request is valid`() {
        val submission = BackendFixtures.submission(id = 1L, userId = 1L)
        val problemSet = BackendFixtures.problemSet(id = 7L, userIdRoleMap = mapOf(2L to UserRole.MODERATOR))
        val supervisor = BackendFixtures.user(id = 2L, name = "moderator")
        val ticket = BackendFixtures.supervisorTicket(submissionId = 1L, problemSetId = 7L, supervisorId = 2L)
        val request = BackendFixtures.createSupervisorTicketRequest()

        val stub = BackendFixtures.checkResult(id = 10L, submissionId = 1L, status = CheckStatus.PENDING)

        every { submissionService.getSubmissionIfOwns(1L, 1L) } returns Mono.just(submission)
        every { problemSetService.findByShareCode("SHARE123") } returns Mono.just(problemSet)
        every { userService.findUserByName("moderator") } returns Mono.just(supervisor)
        every { problemSetService.run { any<Mono<ProblemSet>>().assertIsModeratorOf(2L) } } returns Mono.just(problemSet)
        every { checkResultService.saveCheckResult(any()) } returns Mono.just(stub)
        every { supervisorTicketRepository.save(any()) } returns Mono.just(ticket)
        every { notifier.notify(any()) } returns Mono.just("ok")

        authenticatedClient()
            .post()
            .uri("/api/v1/checker/supervisor")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isAccepted
    }

    @Test
    fun `supervisorCheck returns 403 when supervisor is not a moderator`() {
        val submission = BackendFixtures.submission(id = 1L, userId = 1L)
        val problemSet = BackendFixtures.problemSet(id = 7L, userIdRoleMap = mapOf(2L to UserRole.USER))
        val supervisor = BackendFixtures.user(id = 2L, name = "moderator")
        val request = BackendFixtures.createSupervisorTicketRequest()

        every { submissionService.getSubmissionIfOwns(1L, 1L) } returns Mono.just(submission)
        every { problemSetService.findByShareCode("SHARE123") } returns Mono.just(problemSet)
        every { userService.findUserByName("moderator") } returns Mono.just(supervisor)
        every { problemSetService.run { any<Mono<ProblemSet>>().assertIsModeratorOf(2L) } } returns
            Mono.error(ResponseStatusException(HttpStatus.FORBIDDEN))

        authenticatedClient()
            .post()
            .uri("/api/v1/checker/supervisor")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `supervisorCheck returns 404 when problem set not found`() {
        val submission = BackendFixtures.submission(id = 1L, userId = 1L)
        val request = BackendFixtures.createSupervisorTicketRequest()

        every { submissionService.getSubmissionIfOwns(1L, 1L) } returns Mono.just(submission)
        every { problemSetService.findByShareCode("SHARE123") } returns Mono.empty()
        every { userService.findUserByName("moderator") } returns Mono.just(BackendFixtures.user(id = 2L))

        authenticatedClient()
            .post()
            .uri("/api/v1/checker/supervisor")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `supervisorCheck returns 403 when caller does not own submission`() {
        val request = BackendFixtures.createSupervisorTicketRequest()
        every { submissionService.getSubmissionIfOwns(1L, 1L) } returns
            Mono.error(ResponseStatusException(HttpStatus.FORBIDDEN))

        authenticatedClient()
            .post()
            .uri("/api/v1/checker/supervisor")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isForbidden
    }

    // endregion

    // region POST /api/v1/checker/supervisor/{ticketId}/verdict

    @Test
    fun `supervisorVerdict returns 202 when verdict is valid and caller is assigned supervisor`() {
        val ticket = BackendFixtures.supervisorTicket(id = 1L, submissionId = 1L, supervisorId = 1L)
        val checkResult = BackendFixtures.checkResult(submissionId = 1L, status = CheckStatus.MISTAKE)
        val resolvedTicket = ticket.copy(status = SupervisorTicketStatus.RESOLVED)
        val submission = BackendFixtures.submission(id = 1L, userId = 99L, problemId = 5L)
        val problem = BackendFixtures.problem(id = 5L, key = "savchenko/1.1.1")
        val verdict = BackendFixtures.supervisorVerdict()

        every { supervisorTicketRepository.findById(1L) } returns Mono.just(ticket)
        every { checkResultService.promoteWithVerdict(10L, any()) } returns Mono.just(checkResult)
        every { supervisorTicketRepository.save(resolvedTicket) } returns Mono.just(resolvedTicket)
        every { submissionService.findById(1L) } returns Mono.just(submission)
        every { problemRepository.findById(5L) } returns Mono.just(problem)
        every { notifier.notify(any()) } returns Mono.just("ok")

        authenticatedClient(userId = 1L)
            .post()
            .uri("/api/v1/checker/supervisor/1/verdict")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(verdict)
            .exchange()
            .expectStatus()
            .isAccepted
    }

    @Test
    fun `supervisorVerdict returns 403 when caller is not the assigned supervisor`() {
        val ticket = BackendFixtures.supervisorTicket(id = 1L, supervisorId = 99L)
        every { supervisorTicketRepository.findById(1L) } returns Mono.just(ticket)

        authenticatedClient(userId = 1L)
            .post()
            .uri("/api/v1/checker/supervisor/1/verdict")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(BackendFixtures.supervisorVerdict())
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `supervisorVerdict returns 404 when ticket not found`() {
        every { supervisorTicketRepository.findById(999L) } returns Mono.empty()

        authenticatedClient()
            .post()
            .uri("/api/v1/checker/supervisor/999/verdict")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(BackendFixtures.supervisorVerdict())
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `supervisorVerdict returns 400 when status is PENDING`() {
        val verdict = BackendFixtures.supervisorVerdict(status = CheckStatus.PENDING)

        authenticatedClient()
            .post()
            .uri("/api/v1/checker/supervisor/1/verdict")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(verdict)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `supervisorVerdict returns 400 when explanation is blank`() {
        val verdict = BackendFixtures.supervisorVerdict(explanation = "  ")

        authenticatedClient()
            .post()
            .uri("/api/v1/checker/supervisor/1/verdict")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(verdict)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    // endregion

    // region GET /api/v1/checker/supervisor/my-tickets

    @Test
    fun `getMySupervisorTickets returns 200 list for authenticated user`() {
        val ticket = BackendFixtures.supervisorTicket(id = 1L, supervisorId = 1L)
        val dto = BackendFixtures.supervisorTicketDto(id = 1L)

        every { supervisorTicketRepository.findBySupervisorId(1L, any()) } returns Flux.just(ticket)
        every { supervisorTicketMapper.toDto(ticket) } returns Mono.just(dto)

        authenticatedClient(userId = 1L)
            .get()
            .uri("/api/v1/checker/supervisor/my-tickets")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<SupervisorTicketDto>()
            .hasSize(1)
    }

    @Test
    fun `getMySupervisorTickets returns 200 filtered list when problemSetCode provided`() {
        val ticket = BackendFixtures.supervisorTicket(id = 2L, supervisorId = 1L)
        val dto = BackendFixtures.supervisorTicketDto(id = 2L, problemSetShareCode = "MECH-01")

        every { supervisorTicketRepository.findBySupervisorIdAndProblemSetCode(1L, "MECH-01", any()) } returns
            Flux.just(ticket)
        every { supervisorTicketMapper.toDto(ticket) } returns Mono.just(dto)

        authenticatedClient(userId = 1L)
            .get()
            .uri("/api/v1/checker/supervisor/my-tickets?problemSetCode=MECH-01")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<SupervisorTicketDto>()
            .hasSize(1)
    }

    @Test
    fun `getMySupervisorTickets returns 200 empty list when user has no tickets`() {
        every { supervisorTicketRepository.findBySupervisorId(1L, any()) } returns Flux.empty()

        authenticatedClient(userId = 1L)
            .get()
            .uri("/api/v1/checker/supervisor/my-tickets")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<SupervisorTicketDto>()
            .hasSize(0)
    }

    // endregion

    // region GET /api/v1/checker/by-id/{id}

    @Test
    fun `getCheckResultById returns 200 with DTO when found and user owns submission`() {
        val checkResult = BackendFixtures.checkResult(id = 1L, submissionId = 1L)
        val submission = BackendFixtures.submission(id = 1L, userId = 1L)

        every { checkResultService.findById(1L) } returns Mono.just(checkResult)
        every { submissionService.getSubmissionIfOwns(1L, 1L) } returns Mono.just(submission)

        authenticatedClient()
            .get()
            .uri("/api/v1/checker/by-id/1")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.status")
            .isEqualTo("SUCCESS")
    }

    @Test
    fun `getCheckResultById returns 404 when check result not found`() {
        every { checkResultService.findById(999L) } returns Mono.empty()

        authenticatedClient().get().uri("/api/v1/checker/by-id/999").exchange().expectStatus().isNotFound
    }

    // endregion

    // region GET /api/v1/checker/submissions/{submissionId}

    @Test
    fun `getCheckResultsBySubmissionId returns list for owned submission`() {
        val submission = BackendFixtures.submission(id = 1L, userId = 1L)
        val checkResult = BackendFixtures.checkResult(id = 1L, submissionId = 1L)
        every { submissionService.findById(1L) } returns Mono.just(submission)
        every { checkResultService.findAllBySubmissionId(1L) } returns Flux.just(checkResult)

        authenticatedClient()
            .get()
            .uri("/api/v1/checker/submissions/1")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<CheckResultInfo>()
            .hasSize(1)
    }

    // endregion
}
