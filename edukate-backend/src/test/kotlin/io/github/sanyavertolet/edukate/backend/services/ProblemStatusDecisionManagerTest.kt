@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.repositories.UserProblemStatusRepository
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProblemStatusDecisionManagerTest {
    private val userProblemStatusRepository: UserProblemStatusRepository = mockk()
    private lateinit var manager: ProblemStatusDecisionManager

    @BeforeEach
    fun setUp() {
        manager = ProblemStatusDecisionManager(userProblemStatusRepository)
    }

    // region getStatus(userId, problemId)

    @Test
    fun `getStatus returns solved when best is success`() {
        every { userProblemStatusRepository.findByUserIdAndProblemId("user-1", "1.0.0") } returns
            Mono.just(BackendFixtures.userProblemStatus(bestStatus = SubmissionStatus.SUCCESS))

        StepVerifier.create(manager.getStatus("user-1", "1.0.0")).expectNext(Problem.Status.SOLVED).verifyComplete()
    }

    @Test
    fun `getStatus returns failed when best is failed`() {
        every { userProblemStatusRepository.findByUserIdAndProblemId("user-1", "1.0.0") } returns
            Mono.just(BackendFixtures.userProblemStatus(bestStatus = SubmissionStatus.FAILED))

        StepVerifier.create(manager.getStatus("user-1", "1.0.0")).expectNext(Problem.Status.FAILED).verifyComplete()
    }

    @Test
    fun `getStatus returns solving when best is pending`() {
        every { userProblemStatusRepository.findByUserIdAndProblemId("user-1", "1.0.0") } returns
            Mono.just(BackendFixtures.userProblemStatus(bestStatus = SubmissionStatus.PENDING))

        StepVerifier.create(manager.getStatus("user-1", "1.0.0")).expectNext(Problem.Status.SOLVING).verifyComplete()
    }

    @Test
    fun `getStatus returns not solved when no record`() {
        every { userProblemStatusRepository.findByUserIdAndProblemId("user-1", "1.0.0") } returns Mono.empty()

        StepVerifier.create(manager.getStatus("user-1", "1.0.0")).expectNext(Problem.Status.NOT_SOLVED).verifyComplete()
    }

    // endregion

    // region getStatus(problemId, authentication)

    @Test
    fun `getStatusWithAuth null returns not solved`() {
        StepVerifier.create(manager.getStatus("1.0.0", null)).expectNext(Problem.Status.NOT_SOLVED).verifyComplete()
    }

    @Test
    fun `getStatusWithAuth delegates correctly`() {
        val auth = BackendFixtures.mockAuthentication(userId = "user-1")
        every { userProblemStatusRepository.findByUserIdAndProblemId("user-1", "1.0.0") } returns
            Mono.just(BackendFixtures.userProblemStatus(bestStatus = SubmissionStatus.SUCCESS))

        StepVerifier.create(manager.getStatus("1.0.0", auth)).expectNext(Problem.Status.SOLVED).verifyComplete()
    }

    // endregion
}
