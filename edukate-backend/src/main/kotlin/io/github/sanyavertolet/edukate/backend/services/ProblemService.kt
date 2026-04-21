package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.filters.ProblemFilter
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.common.utils.monoId
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
@CacheConfig(cacheNames = ["problems-by-id"])
@Suppress("TooManyFunctions")
class ProblemService(private val problemRepository: ProblemRepository, private val bookService: BookService) {
    fun getFilteredProblems(
        filter: ProblemFilter,
        authentication: Authentication?,
        pageRequest: PageRequest,
    ): Flux<Problem> {
        val effectiveStatus = filter.status.takeIf { it != Problem.Status.NOT_SOLVED || authentication != null }
        val effectiveFilter = filter.copy(status = effectiveStatus)
        return when {
            effectiveStatus != null && authentication == null ->
                Flux.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required to filter by status"))
            effectiveStatus != null ->
                authentication.monoId().flatMapMany { userId -> withFilter(effectiveFilter, userId, pageRequest) }
            else -> withFilter(effectiveFilter, null, pageRequest)
        }
    }

    fun countFilteredProblems(filter: ProblemFilter, authentication: Authentication?): Mono<Long> {
        val effectiveStatus = filter.status.takeIf { it != Problem.Status.NOT_SOLVED || authentication != null }
        val effectiveFilter = filter.copy(status = effectiveStatus)
        return when {
            effectiveStatus != null && authentication == null ->
                ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required to filter by status").toMono()
            effectiveStatus != null -> authentication.monoId().flatMap { userId -> countWithFilter(effectiveFilter, userId) }
            else -> countWithFilter(effectiveFilter, null)
        }
    }

    @Cacheable(key = "#id") fun findProblemById(id: Long): Mono<Problem> = problemRepository.findById(id)

    @Cacheable(cacheNames = ["problems-by-key"], key = "#key")
    fun findProblemByKey(key: String): Mono<Problem> = problemRepository.findByKey(key)

    fun findProblemsByIds(problemIds: List<Long>): Flux<Problem> = problemRepository.findByIdIn(problemIds)

    @Caching(
        evict =
            [
                CacheEvict(cacheNames = ["problems-by-id"], key = "#problem.id", condition = "#problem.id != null"),
                CacheEvict(cacheNames = ["problems-by-key"], key = "#problem.key", condition = "!#problem.key.isBlank()"),
            ]
    )
    fun updateProblem(problem: Problem): Mono<Problem> = enrichWithKey(problem).flatMap { problemRepository.save(it) }

    @Caching(
        evict =
            [
                CacheEvict(cacheNames = ["problems-by-id"], allEntries = true),
                CacheEvict(cacheNames = ["problems-by-key"], allEntries = true),
            ]
    )
    fun updateProblemBatch(problems: Flux<Problem>): Mono<Long> =
        problems.concatMap { enrichWithKey(it) }.let { problemRepository.saveAll(it).count() }

    private fun enrichWithKey(problem: Problem): Mono<Problem> =
        if (problem.key.isNotBlank()) Mono.just(problem)
        else
            bookService.findById(problem.bookId).orNotFound("Book not found for problem ${problem.id}").map { book ->
                problem.copy(key = "${book.slug}/${problem.code}")
            }

    @Caching(
        evict =
            [
                CacheEvict(cacheNames = ["problems-by-id"], key = "#id"),
                CacheEvict(cacheNames = ["problems-by-key"], allEntries = true),
            ]
    )
    fun deleteProblemById(id: Long): Mono<Void> = problemRepository.deleteById(id)

    fun getProblemCodesByPrefix(prefix: String, limit: Int): Flux<String> =
        problemRepository.findByCodeStartingWith(prefix, limit).map { it.code }

    fun getRandomUnsolvedProblemKey(authentication: Authentication?): Mono<String> =
        authentication
            .monoId()
            .flatMap { problemRepository.findRandomUnsolvedProblem(it).map { p -> p.key } }
            .switchIfEmpty(problemRepository.findRandomProblem().map { it.key })

    private fun resolveBookId(slug: String): Mono<Long> =
        bookService.findBySlug(slug).orNotFound("Book not found: $slug").map { requireNotNull(it.id) }

    private fun statusToDbValue(status: Problem.Status?): String? =
        when (status) {
            Problem.Status.SOLVED -> "SUCCESS"
            Problem.Status.FAILED -> "FAILED"
            Problem.Status.SOLVING -> "PENDING"
            Problem.Status.NOT_SOLVED,
            null -> null
        }

    private fun withFilter(filter: ProblemFilter, userId: Long?, pageRequest: PageRequest): Flux<Problem> {
        val prefix = filter.prefix?.takeIf { it.isNotBlank() }
        val notSolved = filter.status == Problem.Status.NOT_SOLVED
        val bestStatus = statusToDbValue(filter.status)
        val limit = pageRequest.pageSize
        val offset = pageRequest.offset
        if (!filter.bookSlug.isNullOrBlank()) {
            return resolveBookId(filter.bookSlug).flatMapMany { bookId ->
                problemRepository.findWithFilter(
                    bookId,
                    prefix,
                    filter.isHard,
                    filter.hasPictures,
                    filter.hasResult,
                    notSolved,
                    bestStatus,
                    userId,
                    limit,
                    offset,
                )
            }
        }
        return problemRepository.findWithFilter(
            null,
            prefix,
            filter.isHard,
            filter.hasPictures,
            filter.hasResult,
            notSolved,
            bestStatus,
            userId,
            limit,
            offset,
        )
    }

    private fun countWithFilter(filter: ProblemFilter, userId: Long?): Mono<Long> {
        val prefix = filter.prefix?.takeIf { it.isNotBlank() }
        val notSolved = filter.status == Problem.Status.NOT_SOLVED
        val bestStatus = statusToDbValue(filter.status)
        if (!filter.bookSlug.isNullOrBlank()) {
            return resolveBookId(filter.bookSlug).flatMap { bookId ->
                problemRepository.countWithFilter(
                    bookId,
                    prefix,
                    filter.isHard,
                    filter.hasPictures,
                    filter.hasResult,
                    notSolved,
                    bestStatus,
                    userId,
                )
            }
        }
        return problemRepository.countWithFilter(
            null,
            prefix,
            filter.isHard,
            filter.hasPictures,
            filter.hasResult,
            notSolved,
            bestStatus,
            userId,
        )
    }
}
