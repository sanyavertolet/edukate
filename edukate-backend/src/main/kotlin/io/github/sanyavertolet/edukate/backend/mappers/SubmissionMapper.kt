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
        Mono.zip(
                collectFileUrls(submission),
                userService.findUserById(submission.userId).map { it.name }.defaultIfEmpty("UNKNOWN"),
                problemService.findProblemById(submission.problemId).map { it.key }.defaultIfEmpty("UNKNOWN"),
            )
            .map { tuple ->
                SubmissionDto(
                    requireNotNull(submission.id) { "Submission ID cannot be null" },
                    tuple.t3,
                    tuple.t2,
                    submission.status,
                    requireNotNull(submission.createdAt) { "Submission creation timestamp cannot be null" },
                    tuple.t1,
                )
            }

    fun prepareContext(submission: Submission): Mono<SubmissionContext> =
        problemService.findProblemById(submission.problemId).flatMap { problem ->
            val problemRawKeys = problem.images.map { ProblemFileKey(requireNotNull(problem.id), it).toString() }
            collectFileKeyPaths(submission).map { submissionRawKeys ->
                SubmissionContext(
                    requireNotNull(submission.id) { "Submission id must not be null" },
                    requireNotNull(problem.id),
                    problem.text,
                    problemRawKeys,
                    submissionRawKeys,
                )
            }
        }

    private fun collectFileUrls(submission: Submission): Mono<List<String>> =
        submission.fileObjectIds.let { ids ->
            if (ids.isEmpty()) return@let Mono.just(emptyList())
            fileObjectRepository
                .findAllById(ids.mapNotNull { it.toLongOrNull() })
                .map { it.key }
                .flatMapSequential { fileManager.getPresignedUrl(it) }
                .collectList()
        }

    private fun collectFileKeyPaths(submission: Submission): Mono<List<String>> =
        submission.fileObjectIds.let { ids ->
            if (ids.isEmpty()) return@let Mono.just(emptyList())
            fileObjectRepository.findAllById(ids.mapNotNull { it.toLongOrNull() }).map { it.keyPath }.collectList()
        }
}
