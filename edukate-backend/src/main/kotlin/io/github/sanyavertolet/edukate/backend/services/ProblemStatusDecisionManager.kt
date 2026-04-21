package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.repositories.ProblemProgressRepository
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.github.sanyavertolet.edukate.common.utils.monoId
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ProblemStatusDecisionManager(private val problemProgressRepository: ProblemProgressRepository) {
    fun getStatus(userId: Long, problemId: Long): Mono<Problem.Status> =
        problemProgressRepository
            .findByUserIdAndProblemId(userId, problemId)
            .map { statusDecision(it.bestStatus) }
            .defaultIfEmpty(Problem.Status.NOT_SOLVED)

    fun getStatus(problemId: Long, authentication: Authentication?): Mono<Problem.Status> =
        authentication.monoId().flatMap { userId -> getStatus(userId, problemId) }.defaultIfEmpty(Problem.Status.NOT_SOLVED)

    private fun statusDecision(bestStatus: SubmissionStatus): Problem.Status =
        when (bestStatus) {
            SubmissionStatus.SUCCESS -> Problem.Status.SOLVED
            SubmissionStatus.FAILED -> Problem.Status.FAILED
            SubmissionStatus.PENDING -> Problem.Status.SOLVING
        }
}
