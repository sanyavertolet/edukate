package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.services.AnswerService
import io.github.sanyavertolet.edukate.backend.services.BookService
import io.github.sanyavertolet.edukate.backend.services.ProblemStatusDecisionManager
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Component
class ProblemMapper(
    private val problemStatusDecisionManager: ProblemStatusDecisionManager,
    private val fileManager: FileManager,
    private val bookService: BookService,
    private val answerService: AnswerService,
) {
    fun toDto(problem: Problem, authentication: Authentication?): Mono<ProblemDto> {
        val problemId = requireNotNull(problem.id)
        return Mono.zip(
                problemStatusDecisionManager.getStatus(problemId, authentication),
                imageUrls(problem).collectList(),
                answerService.hasAnswer(problemId),
                bookService.findById(problem.bookId).orNotFound("Book not found for problem ${problem.id}"),
            )
            .map { tuple ->
                ProblemDto(
                    key = problem.key,
                    code = problem.code,
                    bookSlug = tuple.t4.slug,
                    isHard = problem.isHard,
                    tags = problem.tags,
                    text = problem.text,
                    subtasks = problem.subtasks,
                    images = tuple.t2,
                    status = tuple.t1,
                    hasResult = tuple.t3,
                )
            }
    }

    fun toMetadata(problem: Problem, authentication: Authentication?): Mono<ProblemMetadata> {
        val problemId = requireNotNull(problem.id)
        return Mono.zip(
                problemStatusDecisionManager.getStatus(problemId, authentication),
                bookService.findById(problem.bookId).map { it.slug }.defaultIfEmpty("unknown"),
            )
            .map { tuple ->
                ProblemMetadata(
                    key = problem.key,
                    code = problem.code,
                    bookSlug = tuple.t2,
                    isHard = problem.isHard,
                    tags = problem.tags,
                    status = tuple.t1,
                )
            }
    }

    private fun imageUrls(problem: Problem): Flux<String> =
        problem.images
            .toFlux()
            .map { ProblemFileKey(requireNotNull(problem.id), it) }
            .flatMap { fileManager.getPresignedUrl(it) }
}
