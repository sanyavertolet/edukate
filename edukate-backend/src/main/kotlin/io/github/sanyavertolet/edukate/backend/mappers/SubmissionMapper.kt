package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.dtos.SubmissionDto
import io.github.sanyavertolet.edukate.backend.entities.ProblemLocalization
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.repositories.AnswerLocalizationRepository
import io.github.sanyavertolet.edukate.backend.repositories.AnswerRepository
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemLocalizationRepository
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.ContentLanguage
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
    private val answerRepository: AnswerRepository,
    private val problemLocalizationRepository: ProblemLocalizationRepository,
    private val answerLocalizationRepository: AnswerLocalizationRepository,
) {
    fun toDto(submission: Submission, includeFiles: Boolean = true): Mono<SubmissionDto> =
        Mono.zip(
                if (includeFiles) collectFileUrls(submission) else Mono.just(emptyList()),
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
                    requireNotNull(submission.updatedAt) { "Submission update timestamp cannot be null" },
                    tuple.t1,
                )
            }

    fun prepareContext(submission: Submission, checkResultId: Long): Mono<SubmissionContext> =
        problemService.findProblemById(submission.problemId).flatMap { problem ->
            val problemId = requireNotNull(problem.id)
            val (bookSlug, problemCode) = problem.key.split("/", limit = 2)
            val problemRawKeys = problem.images.map { ProblemFileKey(bookSlug, problemCode, it).toString() }
            val language = submission.language
            Mono.zip(
                    collectFileKeyPaths(submission),
                    findLocalizedProblemText(problemId, language),
                    findLocalizedAnswerText(problemId, language),
                )
                .map { tuple ->
                    SubmissionContext(
                        requireNotNull(submission.id) { "Submission id must not be null" },
                        checkResultId,
                        problemId,
                        tuple.t2,
                        problemRawKeys,
                        tuple.t1,
                        tuple.t3.ifEmpty { null },
                        language,
                    )
                }
        }

    private fun findLocalizedProblemText(problemId: Long, language: ContentLanguage): Mono<String> =
        problemLocalizationRepository
            .findByProblemIdAndLanguage(problemId, language)
            .switchIfEmpty(Mono.defer { problemLocalizationRepository.findFirstByProblemId(problemId) })
            .map { buildProblemText(it) }
            .defaultIfEmpty("")

    private fun buildProblemText(loc: ProblemLocalization): String = buildString {
        if (loc.text.isNotBlank()) append(loc.text)
        loc.subproblems.forEach { sub ->
            if (isNotEmpty()) append("\n\n")
            append("${sub.code}) ${sub.text}")
        }
    }

    private fun findLocalizedAnswerText(problemId: Long, language: ContentLanguage): Mono<String> =
        answerRepository
            .findByProblemId(problemId)
            .flatMap { answer ->
                answerLocalizationRepository
                    .findByAnswerIdAndLanguage(requireNotNull(answer.id), language)
                    .switchIfEmpty(
                        Mono.defer { answerLocalizationRepository.findFirstByAnswerId(requireNotNull(answer.id)) }
                    )
                    .map { it.text }
            }
            .defaultIfEmpty("")

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
