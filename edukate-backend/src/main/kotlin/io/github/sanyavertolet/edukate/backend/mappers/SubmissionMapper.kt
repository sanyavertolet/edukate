package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.dtos.SubmissionDto
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.checks.SubmissionContext
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SubmissionMapper(
    private val fileObjectRepository: FileObjectRepository,
    private val fileManager: FileManager,
    private val userService: UserService,
    private val problemService: ProblemService,
) {
    fun toDto(submission: Submission): Mono<SubmissionDto> =
        collectFileUrls(submission).zipWith(
            userService.findUserById(submission.userId).map { it.name }.defaultIfEmpty("UNKNOWN")
        ) { fileUrls, userName ->
            SubmissionDto(
                requireNotNull(submission.id) { "Submission ID cannot be null" },
                submission.problemId,
                userName,
                submission.status,
                requireNotNull(submission.createdAt) { "Submission creation timestamp cannot be null" },
                fileUrls,
            )
        }

    fun prepareContext(submission: Submission): Mono<SubmissionContext> =
        problemService.findProblemById(submission.problemId).flatMap { problem ->
            val problemRawKeys = problem.images.map { ProblemFileKey(problem.id, it).toString() }
            fileObjectRepository
                .findAllById(submission.fileObjectIds)
                .map { it.keyPath }
                .collectList()
                .map { submissionRawKeys ->
                    SubmissionContext(
                        requireNotNull(submission.id) { "Submission id must not be null" },
                        problem.id,
                        problem.text,
                        problemRawKeys,
                        submissionRawKeys,
                    )
                }
        }

    private fun collectFileUrls(submission: Submission): Mono<List<String>> =
        fileObjectRepository
            .findAllById(submission.fileObjectIds)
            .map { it.key }
            .flatMapSequential { fileManager.getPresignedUrl(it) }
            .collectList()
}
