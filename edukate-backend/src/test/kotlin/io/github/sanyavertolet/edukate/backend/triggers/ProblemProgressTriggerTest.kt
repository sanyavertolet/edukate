package io.github.sanyavertolet.edukate.backend.triggers

import io.github.sanyavertolet.edukate.backend.AbstractBackendIntegrationTest
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.CheckResult
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.repositories.BookRepository
import io.github.sanyavertolet.edukate.backend.repositories.CheckResultRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemProgressRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.SubmissionRepository
import io.github.sanyavertolet.edukate.backend.repositories.UserRepository
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests that verify the PL/pgSQL trigger chain defined in V1__create_schema.sql: on_check_result_change → syncs
 * submissions.status from best non-PENDING check_result status on_submission_status_change → upserts problem_progress when
 * submission status is promoted
 */
class ProblemProgressTriggerTest : AbstractBackendIntegrationTest() {

    @Autowired private lateinit var submissionRepository: SubmissionRepository
    @Autowired private lateinit var checkResultRepository: CheckResultRepository
    @Autowired private lateinit var problemProgressRepository: ProblemProgressRepository
    @Autowired private lateinit var problemRepository: ProblemRepository
    @Autowired private lateinit var bookRepository: BookRepository
    @Autowired private lateinit var userRepository: UserRepository

    private var userId: Long = 0L
    private var problemId: Long = 0L

    @BeforeEach
    fun setUpFixtures() {
        val book = bookRepository.save(BackendFixtures.book(id = null, slug = "trigger-test-book")).block()!!
        val problem = problemRepository.save(BackendFixtures.problem(id = null, bookId = requireNotNull(book.id))).block()!!
        val user = userRepository.save(BackendFixtures.user(id = null, name = "trigger-test-user")).block()!!
        userId = requireNotNull(user.id)
        problemId = requireNotNull(problem.id)
    }

    @AfterEach
    fun tearDownFixtures() {
        // Delete in FK order: check_results → problem_progress → submissions → problems → books →
        // users
        checkResultRepository.deleteAll().block()
        problemProgressRepository.deleteAll().block()
        submissionRepository.deleteAll().block()
        problemRepository.deleteAll().block()
        bookRepository.deleteAll().block()
        userRepository.deleteAll().block()
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private fun newSubmission(): Submission = submissionRepository.save(Submission.of(problemId, userId)).block()!!

    private fun insertCheckResult(submissionId: Long, status: CheckStatus): CheckResult =
        checkResultRepository
            .save(
                CheckResult(
                    submissionId = submissionId,
                    status = status,
                    trustLevel = 0.9f,
                    errorType = CheckErrorType.NONE,
                    explanation = "trigger test",
                )
            )
            .block()!!

    // ── tests ─────────────────────────────────────────────────────────────────────

    @Test
    fun `PENDING stub does not create problem_progress and leaves submission PENDING`() {
        val sub = newSubmission()
        val subId = requireNotNull(sub.id)

        insertCheckResult(subId, CheckStatus.PENDING)

        val reloaded = submissionRepository.findById(subId).block()
        assertThat(reloaded?.status).isEqualTo(SubmissionStatus.PENDING)

        val progress = problemProgressRepository.findByUserIdAndProblemId(userId, problemId).block()
        assertThat(progress).isNull()
    }

    @Test
    fun `SUCCESS check result updates submission to SUCCESS and creates progress with bestStatus SUCCESS`() {
        val sub = newSubmission()
        val subId = requireNotNull(sub.id)

        insertCheckResult(subId, CheckStatus.SUCCESS)

        val reloaded = submissionRepository.findById(subId).block()
        assertThat(reloaded?.status).isEqualTo(SubmissionStatus.SUCCESS)

        val progress = problemProgressRepository.findByUserIdAndProblemId(userId, problemId).block()
        assertThat(progress).isNotNull
        assertThat(progress?.bestStatus).isEqualTo(SubmissionStatus.SUCCESS)
        assertThat(progress?.latestStatus).isEqualTo(SubmissionStatus.SUCCESS)
        assertThat(progress?.bestSubmissionId).isEqualTo(subId)
    }

    @Test
    fun `MISTAKE check result updates submission to FAILED and creates progress with bestStatus FAILED`() {
        val sub = newSubmission()
        val subId = requireNotNull(sub.id)

        insertCheckResult(subId, CheckStatus.MISTAKE)

        val reloaded = submissionRepository.findById(subId).block()
        assertThat(reloaded?.status).isEqualTo(SubmissionStatus.FAILED)

        val progress = problemProgressRepository.findByUserIdAndProblemId(userId, problemId).block()
        assertThat(progress).isNotNull
        assertThat(progress?.bestStatus).isEqualTo(SubmissionStatus.FAILED)
        assertThat(progress?.bestSubmissionId).isEqualTo(subId)
    }

    @Test
    fun `INTERNAL_ERROR check result updates submission to FAILED`() {
        val sub = newSubmission()
        val subId = requireNotNull(sub.id)

        insertCheckResult(subId, CheckStatus.INTERNAL_ERROR)

        val reloaded = submissionRepository.findById(subId).block()
        assertThat(reloaded?.status).isEqualTo(SubmissionStatus.FAILED)

        val progress = problemProgressRepository.findByUserIdAndProblemId(userId, problemId).block()
        assertThat(progress).isNotNull
        assertThat(progress?.bestStatus).isEqualTo(SubmissionStatus.FAILED)
    }

    @Test
    fun `FAILED progress is promoted to SUCCESS when a SUCCESS check result arrives`() {
        val sub1 = newSubmission()
        insertCheckResult(requireNotNull(sub1.id), CheckStatus.MISTAKE)

        val progressAfterFailed = problemProgressRepository.findByUserIdAndProblemId(userId, problemId).block()
        assertThat(progressAfterFailed?.bestStatus).isEqualTo(SubmissionStatus.FAILED)

        val sub2 = newSubmission()
        insertCheckResult(requireNotNull(sub2.id), CheckStatus.SUCCESS)

        val progressAfterSuccess = problemProgressRepository.findByUserIdAndProblemId(userId, problemId).block()
        assertThat(progressAfterSuccess?.bestStatus).isEqualTo(SubmissionStatus.SUCCESS)
        assertThat(progressAfterSuccess?.bestSubmissionId).isEqualTo(sub2.id)
    }

    @Test
    fun `best submission is unchanged when a second FAILED check arrives after first FAILED`() {
        val sub1 = newSubmission()
        insertCheckResult(requireNotNull(sub1.id), CheckStatus.MISTAKE)

        val progressAfterFirst = problemProgressRepository.findByUserIdAndProblemId(userId, problemId).block()
        val bestSubmissionIdBefore = progressAfterFirst?.bestSubmissionId

        val sub2 = newSubmission()
        insertCheckResult(requireNotNull(sub2.id), CheckStatus.MISTAKE)

        val progressAfterSecond = problemProgressRepository.findByUserIdAndProblemId(userId, problemId).block()
        assertThat(progressAfterSecond?.bestStatus).isEqualTo(SubmissionStatus.FAILED)
        // best stays the earliest FAILED (sub1) — SQL tiebreaker: ORDER BY created_at ASC
        assertThat(progressAfterSecond?.bestSubmissionId).isEqualTo(bestSubmissionIdBefore)
        // latest updates to the newest submission
        assertThat(progressAfterSecond?.latestSubmissionId).isEqualTo(sub2.id)
    }

    @Test
    fun `SUCCESS progress is not demoted by a subsequent MISTAKE check`() {
        val sub1 = newSubmission()
        insertCheckResult(requireNotNull(sub1.id), CheckStatus.SUCCESS)

        val progressAfterSuccess = problemProgressRepository.findByUserIdAndProblemId(userId, problemId).block()
        assertThat(progressAfterSuccess?.bestStatus).isEqualTo(SubmissionStatus.SUCCESS)

        val sub2 = newSubmission()
        insertCheckResult(requireNotNull(sub2.id), CheckStatus.MISTAKE)

        val progressAfterMistake = problemProgressRepository.findByUserIdAndProblemId(userId, problemId).block()
        assertThat(progressAfterMistake?.bestStatus).isEqualTo(SubmissionStatus.SUCCESS)
        assertThat(progressAfterMistake?.bestSubmissionId).isEqualTo(sub1.id)
    }
}
