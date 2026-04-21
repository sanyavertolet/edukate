package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.ProblemProgress
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.repositories.ProblemProgressRepository
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ProblemProgressService(private val problemProgressRepository: ProblemProgressRepository) {
    fun updateProgress(submission: Submission): Mono<ProblemProgress> {
        val submissionId = requireNotNull(submission.id) { "Submission ID must not be null" }
        val createdAt = requireNotNull(submission.createdAt) { "Submission createdAt must not be null" }

        return problemProgressRepository
            .findByUserIdAndProblemId(submission.userId, submission.problemId)
            .flatMap { existing ->
                val shouldUpdateBest =
                    isBetterStatus(submission.status, existing.bestStatus) ||
                        (submission.status == existing.bestStatus && createdAt < existing.bestTime)
                val updated =
                    existing.copy(
                        latestStatus = submission.status,
                        latestTime = createdAt,
                        latestSubmissionId = submissionId,
                        bestStatus = if (shouldUpdateBest) submission.status else existing.bestStatus,
                        bestTime = if (shouldUpdateBest) createdAt else existing.bestTime,
                        bestSubmissionId = if (shouldUpdateBest) submissionId else existing.bestSubmissionId,
                    )
                problemProgressRepository.save(updated)
            }
            .switchIfEmpty(
                problemProgressRepository.save(
                    ProblemProgress(
                        userId = submission.userId,
                        problemId = submission.problemId,
                        latestStatus = submission.status,
                        latestTime = createdAt,
                        latestSubmissionId = submissionId,
                        bestStatus = submission.status,
                        bestTime = createdAt,
                        bestSubmissionId = submissionId,
                    )
                )
            )
            .doOnNext { log.debug("problem_progress upserted for submission {}", submissionId) }
            .doOnError { ex -> log.error("problem_progress upsert failed for submission {}", submissionId, ex) }
    }

    private fun isBetterStatus(newStatus: SubmissionStatus, existingStatus: SubmissionStatus): Boolean =
        statusRank(newStatus) > statusRank(existingStatus)

    private fun statusRank(status: SubmissionStatus): Int =
        when (status) {
            SubmissionStatus.PENDING -> 0
            SubmissionStatus.FAILED -> 1
            SubmissionStatus.SUCCESS -> 2
        }

    companion object {
        private val log = LoggerFactory.getLogger(ProblemProgressService::class.java)
    }
}
