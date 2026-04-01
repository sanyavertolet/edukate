package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.backend.utils.SemVerUtils.semVerSort
import io.github.sanyavertolet.edukate.common.utils.monoId
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
@Suppress("TooManyFunctions")
class ProblemService(
    private val problemRepository: ProblemRepository,
    private val fileManager: FileManager,
    private val problemStatusDecisionManager: ProblemStatusDecisionManager,
) {
    fun getFilteredProblems(pageRequest: PageRequest): Flux<Problem> =
        problemRepository.findAll(pageRequest.withSort(semVerSort()))

    fun findProblemById(id: String): Mono<Problem> = problemRepository.findById(id)

    fun findProblemsByIds(problemIds: List<String>): Flux<Problem> = problemRepository.findProblemsByIdIn(problemIds)

    fun updateProblem(problem: Problem): Mono<Problem> = problemRepository.save(problem)

    fun updateProblemBatch(problems: List<Problem>): Flux<Problem> =
        Flux.fromIterable(problems).flatMap { problemRepository.save(it) }

    fun countProblems(): Mono<Long> = problemRepository.count()

    fun deleteProblemById(id: String): Mono<Void> = problemRepository.deleteById(id)

    fun getProblemIdsByPrefix(prefix: String, limit: Int): Flux<String> =
        problemRepository.findProblemsByIdStartingWith(prefix, Pageable.ofSize(limit)).map { it.id }

    fun getRandomUnsolvedProblemId(authentication: Authentication?): Mono<String> =
        authentication
            .monoId()
            .flatMap { problemRepository.findRandomUnsolvedProblemId(it) }
            .switchIfEmpty(problemRepository.findRandomProblemId())

    fun problemImageDownloadUrls(problemId: String, images: List<String>): Flux<String> =
        Flux.fromIterable(images)
            .map { fileName -> ProblemFileKey(problemId, fileName) }
            .flatMap { fileManager.getPresignedUrl(it) }

    fun prepareDto(problem: Problem, authentication: Authentication?): Mono<ProblemDto> =
        problemStatusDecisionManager
            .getStatus(problem.id, authentication)
            .zipWith(problemImageDownloadUrls(problem.id, problem.images).collectList(), problem::toProblemDto)

    fun prepareMetadata(problem: Problem, authentication: Authentication?): Mono<ProblemMetadata> =
        problemStatusDecisionManager.getStatus(problem.id, authentication).map { problem.toProblemMetadata(it) }
}
