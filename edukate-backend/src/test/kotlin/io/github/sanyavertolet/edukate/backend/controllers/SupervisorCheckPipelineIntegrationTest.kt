package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.AbstractBackendIntegrationTest
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.ProblemSetProblem
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.repositories.BookRepository
import io.github.sanyavertolet.edukate.backend.repositories.CheckResultRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemProgressRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetRepository
import io.github.sanyavertolet.edukate.backend.repositories.SubmissionRepository
import io.github.sanyavertolet.edukate.backend.repositories.SupervisorTicketRepository
import io.github.sanyavertolet.edukate.backend.repositories.UserRepository
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.github.sanyavertolet.edukate.common.checks.SupervisorTicketStatus
import io.github.sanyavertolet.edukate.common.users.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType

/**
 * Integration tests for the two-phase supervisor check pipeline.
 *
 * Phase 1: POST /api/v1/checker/supervisor → creates SupervisorTicket (PENDING) + CheckResult stub (PENDING) → submission
 * stays PENDING (DB trigger does not fire for PENDING check results)
 *
 * Phase 2: POST /api/v1/checker/supervisor/{ticketId}/verdict → promotes CheckResult to SUCCESS/MISTAKE with trustLevel =
 * 1.0 → marks SupervisorTicket RESOLVED → DB trigger syncs Submission status from the promoted CheckResult
 */
class SupervisorCheckPipelineIntegrationTest : AbstractBackendIntegrationTest() {

    @Autowired private lateinit var cacheManager: CacheManager
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var bookRepository: BookRepository
    @Autowired private lateinit var problemRepository: ProblemRepository
    @Autowired private lateinit var problemSetRepository: ProblemSetRepository
    @Autowired private lateinit var problemSetProblemRepository: ProblemSetProblemRepository
    @Autowired private lateinit var submissionRepository: SubmissionRepository
    @Autowired private lateinit var checkResultRepository: CheckResultRepository
    @Autowired private lateinit var supervisorTicketRepository: SupervisorTicketRepository
    @Autowired private lateinit var problemProgressRepository: ProblemProgressRepository

    private var ownerId: Long = 0L
    private var supervisorId: Long = 0L
    private var submissionId: Long = 0L
    private val problemSetShareCode = "PIPE-TEST-CODE"
    private val ownerName = "pipe-owner"
    private val supervisorName = "pipe-supervisor"

    private val resultSort = Sort.by(Sort.Direction.DESC, "createdAt")

    @BeforeEach
    fun setUpFixtures() {
        val owner = userRepository.save(BackendFixtures.user(id = null, name = ownerName)).block()!!
        val supervisor = userRepository.save(BackendFixtures.user(id = null, name = supervisorName)).block()!!
        ownerId = requireNotNull(owner.id)
        supervisorId = requireNotNull(supervisor.id)

        val book = bookRepository.save(BackendFixtures.book(id = null, slug = "pipe-test-book")).block()!!
        val problem = problemRepository.save(BackendFixtures.problem(id = null, bookId = requireNotNull(book.id))).block()!!
        val problemId = requireNotNull(problem.id)

        val problemSet =
            problemSetRepository
                .save(
                    BackendFixtures.problemSet(
                        id = null,
                        shareCode = problemSetShareCode,
                        userIdRoleMap = mapOf(ownerId to UserRole.USER, supervisorId to UserRole.MODERATOR),
                    )
                )
                .block()!!
        problemSetProblemRepository.save(ProblemSetProblem(requireNotNull(problemSet.id), problemId, 0)).block()

        val submission = submissionRepository.save(Submission.of(problemId, ownerId)).block()!!
        submissionId = requireNotNull(submission.id)
    }

    @AfterEach
    fun tearDownFixtures() {
        // Evict all Caffeine caches so stale entries (e.g., problemSets keyed by shareCode,
        // users keyed by name/id) don't survive into the next test's @BeforeEach setup.
        cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
        // Delete in FK-safe order to avoid constraint violations.
        supervisorTicketRepository.deleteAll().block()
        checkResultRepository.deleteAll().block()
        problemProgressRepository.deleteAll().block()
        submissionRepository.deleteAll().block()
        problemSetProblemRepository.deleteAll().block()
        problemSetRepository.deleteAll().block()
        problemRepository.deleteAll().block()
        bookRepository.deleteAll().block()
        userRepository.deleteAll().block()
    }

    private fun ticketRequest() =
        BackendFixtures.createSupervisorTicketRequest(
            submissionId = submissionId,
            problemSetCode = problemSetShareCode,
            supervisorName = supervisorName,
        )

    // ── Phase 1 tests ─────────────────────────────────────────────────────────────

    @Test
    fun `phase 1 - ticket creation returns 202 and stores PENDING ticket and stub`() {
        authenticatedClient(ownerId, ownerName)
            .post()
            .uri("/api/v1/checker/supervisor")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ticketRequest())
            .exchange()
            .expectStatus()
            .isAccepted

        val tickets = supervisorTicketRepository.findAll().collectList().block()!!
        assertThat(tickets).hasSize(1)
        assertThat(tickets[0].supervisorId).isEqualTo(supervisorId)
        assertThat(tickets[0].submissionId).isEqualTo(submissionId)
        assertThat(tickets[0].status).isEqualTo(SupervisorTicketStatus.PENDING)

        val results = checkResultRepository.findBySubmissionId(submissionId, resultSort).collectList().block()!!
        assertThat(results).hasSize(1)
        assertThat(results[0].status).isEqualTo(CheckStatus.PENDING)
        assertThat(results[0].trustLevel).isEqualTo(0.0f)
    }

    @Test
    fun `phase 1 - submission stays PENDING after ticket creation`() {
        authenticatedClient(ownerId, ownerName)
            .post()
            .uri("/api/v1/checker/supervisor")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ticketRequest())
            .exchange()
            .expectStatus()
            .isAccepted

        val submission = submissionRepository.findById(submissionId).block()!!
        assertThat(submission.status).isEqualTo(SubmissionStatus.PENDING)
    }

    @Test
    fun `phase 1 - non-owner cannot create ticket for another user's submission`() {
        authenticatedClient(supervisorId, supervisorName)
            .post()
            .uri("/api/v1/checker/supervisor")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ticketRequest())
            .exchange()
            .expectStatus()
            .isForbidden

        assertThat(supervisorTicketRepository.findAll().collectList().block()!!).isEmpty()
    }

    // ── Phase 2 tests ─────────────────────────────────────────────────────────────

    @Test
    fun `phase 2 - SUCCESS verdict promotes check result and syncs submission to SUCCESS via DB trigger`() {
        // Phase 1
        authenticatedClient(ownerId, ownerName)
            .post()
            .uri("/api/v1/checker/supervisor")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ticketRequest())
            .exchange()
            .expectStatus()
            .isAccepted

        val ticketId = requireNotNull(supervisorTicketRepository.findAll().collectList().block()!!.first().id)

        // Phase 2: submit SUCCESS verdict
        authenticatedClient(supervisorId, supervisorName)
            .post()
            .uri("/api/v1/checker/supervisor/$ticketId/verdict")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(BackendFixtures.supervisorVerdict(status = CheckStatus.SUCCESS))
            .exchange()
            .expectStatus()
            .isAccepted

        val results = checkResultRepository.findBySubmissionId(submissionId, resultSort).collectList().block()!!
        assertThat(results).hasSize(1)
        assertThat(results[0].status).isEqualTo(CheckStatus.SUCCESS)
        assertThat(results[0].trustLevel).isEqualTo(1.0f)

        val ticket = supervisorTicketRepository.findById(ticketId).block()!!
        assertThat(ticket.status).isEqualTo(SupervisorTicketStatus.RESOLVED)

        // DB trigger: SUCCESS check result → submission status = SUCCESS
        val submission = submissionRepository.findById(submissionId).block()!!
        assertThat(submission.status).isEqualTo(SubmissionStatus.SUCCESS)
    }

    @Test
    fun `phase 2 - MISTAKE verdict promotes check result and syncs submission to FAILED via DB trigger`() {
        // Phase 1
        authenticatedClient(ownerId, ownerName)
            .post()
            .uri("/api/v1/checker/supervisor")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ticketRequest())
            .exchange()
            .expectStatus()
            .isAccepted

        val ticketId = requireNotNull(supervisorTicketRepository.findAll().collectList().block()!!.first().id)

        // Phase 2: submit MISTAKE verdict
        val verdict = BackendFixtures.supervisorVerdict(status = CheckStatus.MISTAKE, errorType = CheckErrorType.ALGEBRAIC)
        authenticatedClient(supervisorId, supervisorName)
            .post()
            .uri("/api/v1/checker/supervisor/$ticketId/verdict")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(verdict)
            .exchange()
            .expectStatus()
            .isAccepted

        val results = checkResultRepository.findBySubmissionId(submissionId, resultSort).collectList().block()!!
        assertThat(results).hasSize(1)
        assertThat(results[0].status).isEqualTo(CheckStatus.MISTAKE)
        assertThat(results[0].trustLevel).isEqualTo(1.0f)
        assertThat(results[0].errorType).isEqualTo(CheckErrorType.ALGEBRAIC)

        val ticket = supervisorTicketRepository.findById(ticketId).block()!!
        assertThat(ticket.status).isEqualTo(SupervisorTicketStatus.RESOLVED)

        // DB trigger: MISTAKE check result → submission status = FAILED
        val submission = submissionRepository.findById(submissionId).block()!!
        assertThat(submission.status).isEqualTo(SubmissionStatus.FAILED)
    }

    @Test
    fun `phase 2 - non-assigned supervisor cannot submit verdict`() {
        // Phase 1
        authenticatedClient(ownerId, ownerName)
            .post()
            .uri("/api/v1/checker/supervisor")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ticketRequest())
            .exchange()
            .expectStatus()
            .isAccepted

        val ticketId = requireNotNull(supervisorTicketRepository.findAll().collectList().block()!!.first().id)

        // Owner tries to submit the verdict (not the assigned supervisor)
        authenticatedClient(ownerId, ownerName)
            .post()
            .uri("/api/v1/checker/supervisor/$ticketId/verdict")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(BackendFixtures.supervisorVerdict())
            .exchange()
            .expectStatus()
            .isForbidden

        // Stub remains PENDING
        val results = checkResultRepository.findBySubmissionId(submissionId, resultSort).collectList().block()!!
        assertThat(results).hasSize(1)
        assertThat(results[0].status).isEqualTo(CheckStatus.PENDING)

        val ticket = supervisorTicketRepository.findById(ticketId).block()!!
        assertThat(ticket.status).isEqualTo(SupervisorTicketStatus.PENDING)
    }

    @Test
    fun `full pipeline - problem progress is created after SUCCESS verdict`() {
        // Phase 1
        authenticatedClient(ownerId, ownerName)
            .post()
            .uri("/api/v1/checker/supervisor")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ticketRequest())
            .exchange()
            .expectStatus()
            .isAccepted

        val ticketId = requireNotNull(supervisorTicketRepository.findAll().collectList().block()!!.first().id)

        // Verify no problem progress exists yet (PENDING stub doesn't fire the trigger)
        val submission = submissionRepository.findById(submissionId).block()!!
        val progressBefore = problemProgressRepository.findByUserIdAndProblemId(ownerId, submission.problemId).block()
        assertThat(progressBefore).isNull()

        // Phase 2: SUCCESS verdict
        authenticatedClient(supervisorId, supervisorName)
            .post()
            .uri("/api/v1/checker/supervisor/$ticketId/verdict")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(BackendFixtures.supervisorVerdict(status = CheckStatus.SUCCESS))
            .exchange()
            .expectStatus()
            .isAccepted

        // After promotion the DB trigger chain fires: check_result → submission → problem_progress
        val progressAfter = problemProgressRepository.findByUserIdAndProblemId(ownerId, submission.problemId).block()
        assertThat(progressAfter).isNotNull
        assertThat(progressAfter!!.bestStatus).isEqualTo(SubmissionStatus.SUCCESS)
        assertThat(progressAfter.bestSubmissionId).isEqualTo(submissionId)
    }
}
