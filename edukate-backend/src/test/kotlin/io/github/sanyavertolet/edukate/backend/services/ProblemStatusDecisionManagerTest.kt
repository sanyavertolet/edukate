@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.repositories.ProblemProgressRepository
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProblemStatusDecisionManagerTest {
    private val problemProgressRepository: ProblemProgressRepository = mockk()
    private lateinit var manager: ProblemStatusDecisionManager

    @BeforeEach
    fun setUp() {
        manager = ProblemStatusDecisionManager(problemProgressRepository)
    }

    // region getStatus(userId, problemId)

    @Test
    fun `getStatus returns solved when best is success`() {
        every { problemProgressRepository.findByUserIdAndProblemId(1L, 1L) } returns
            Mono.just(BackendFixtures.problemProgress(bestStatus = SubmissionStatus.SUCCESS))

        StepVerifier.create(manager.getStatus(1L, 1L)).expectNext(Problem.Status.SOLVED).verifyComplete()
    }

    @Test
    fun `getStatus returns failed when best is failed`() {
        every { problemProgressRepository.findByUserIdAndProblemId(1L, 1L) } returns
            Mono.just(BackendFixtures.problemProgress(bestStatus = SubmissionStatus.FAILED))

        StepVerifier.create(manager.getStatus(1L, 1L)).expectNext(Problem.Status.FAILED).verifyComplete()
    }

    @Test
    fun `getStatus returns solving when best is pending`() {
        every { problemProgressRepository.findByUserIdAndProblemId(1L, 1L) } returns
            Mono.just(BackendFixtures.problemProgress(bestStatus = SubmissionStatus.PENDING))

        StepVerifier.create(manager.getStatus(1L, 1L)).expectNext(Problem.Status.SOLVING).verifyComplete()
    }

    @Test
    fun `getStatus returns not solved when no record`() {
        every { problemProgressRepository.findByUserIdAndProblemId(1L, 1L) } returns Mono.empty()

        StepVerifier.create(manager.getStatus(1L, 1L)).expectNext(Problem.Status.NOT_SOLVED).verifyComplete()
    }

    // endregion

    // region getStatus(problemId, authentication)

    @Test
    fun `getStatusWithAuth null returns not solved`() {
        StepVerifier.create(manager.getStatus(1L, null)).expectNext(Problem.Status.NOT_SOLVED).verifyComplete()
    }

    @Test
    fun `getStatusWithAuth delegates correctly`() {
        val auth = BackendFixtures.mockAuthentication(userId = 1L)
        every { problemProgressRepository.findByUserIdAndProblemId(1L, 1L) } returns
            Mono.just(BackendFixtures.problemProgress(bestStatus = SubmissionStatus.SUCCESS))

        StepVerifier.create(manager.getStatus(1L, auth)).expectNext(Problem.Status.SOLVED).verifyComplete()
    }

    // endregion
}
