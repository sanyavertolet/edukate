package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.filters.ProblemFilter
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.utils.SemVerUtils.semVerSort
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.github.sanyavertolet.edukate.common.utils.monoId
import org.bson.Document
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
@CacheConfig(cacheNames = ["problems"])
@Suppress("TooManyFunctions")
class ProblemService(private val problemRepository: ProblemRepository, private val mongoTemplate: ReactiveMongoTemplate) {
    fun getFilteredProblems(
        filter: ProblemFilter,
        authentication: Authentication?,
        pageRequest: PageRequest,
    ): Flux<Problem> {
        // For unauthenticated users, NOT_SOLVED is a no-op (all problems qualify), so strip it.
        val effectiveStatus = filter.status.takeIf { it != Problem.Status.NOT_SOLVED || authentication != null }
        val effectiveFilter = filter.copy(status = effectiveStatus)
        return when {
            // Only prefix filtering — skip the aggregation pipeline entirely
            !effectiveFilter.requiresAggregation() -> withPrefixFilter(filter.prefix, pageRequest)
            // Status was stripped (NOT_SOLVED for anonymous) — aggregate without user lookup
            effectiveStatus == null -> findByFilter(effectiveFilter, null, pageRequest)
            // Status filter requires a user but none is authenticated — reject with 401
            authentication == null ->
                Flux.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required to filter by status"))
            // Full aggregation with per-user status lookup
            else -> authentication.monoId().flatMapMany { userId -> findByFilter(effectiveFilter, userId, pageRequest) }
        }
    }

    fun countFilteredProblems(filter: ProblemFilter, authentication: Authentication?): Mono<Long> {
        val effectiveStatus = filter.status.takeIf { it != Problem.Status.NOT_SOLVED || authentication != null }
        val effectiveFilter = filter.copy(status = effectiveStatus)
        return when {
            // Only prefix filtering — skip the aggregation pipeline entirely
            !effectiveFilter.requiresAggregation() -> countWithPrefixFilter(filter.prefix)
            // Status was stripped (NOT_SOLVED for anonymous) — aggregate without user lookup
            effectiveStatus == null -> countByFilter(effectiveFilter, null)
            // Status filter requires a user but none is authenticated — reject with 401
            authentication == null ->
                ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required to filter by status").toMono()
            // Full aggregation with per-user status lookup
            else -> authentication.monoId().flatMap { userId -> countByFilter(effectiveFilter, userId) }
        }
    }

    @Cacheable(key = "#id") fun findProblemById(id: String): Mono<Problem> = problemRepository.findById(id)

    fun findProblemsByIds(problemIds: List<String>): Flux<Problem> = problemRepository.findProblemsByIdIn(problemIds)

    @CacheEvict(key = "#problem.id") fun updateProblem(problem: Problem): Mono<Problem> = problemRepository.save(problem)

    @CacheEvict(allEntries = true)
    fun updateProblemBatch(problems: Flux<Problem>): Mono<Long> = problemRepository.saveAll(problems).count()

    @CacheEvict(key = "#id") fun deleteProblemById(id: String): Mono<Void> = problemRepository.deleteById(id)

    fun getProblemIdsByPrefix(prefix: String, limit: Int): Flux<String> =
        problemRepository.findProblemsByIdStartingWith(prefix, Pageable.ofSize(limit)).map { it.id }

    fun getRandomUnsolvedProblemId(authentication: Authentication?): Mono<String> =
        authentication
            .monoId()
            .flatMap { problemRepository.findRandomUnsolvedProblemId(it) }
            .switchIfEmpty(problemRepository.findRandomProblemId())

    private fun findByFilter(filter: ProblemFilter, userId: String?, pageRequest: PageRequest): Flux<Problem> {
        val operations = buildFilterStages(filter, userId)
        operations += Aggregation.sort(semVerSort())
        val offset = pageRequest.pageNumber.toLong() * pageRequest.pageSize
        operations += Aggregation.skip(offset)
        operations += Aggregation.limit(pageRequest.pageSize.toLong())
        return mongoTemplate.aggregate<Problem>(Aggregation.newAggregation(Problem::class.java, operations))
    }

    private fun countByFilter(filter: ProblemFilter, userId: String?): Mono<Long> {
        val operations = buildFilterStages(filter, userId)
        operations += Aggregation.count().`as`("count")
        return mongoTemplate
            .aggregate<Document>(Aggregation.newAggregation(Problem::class.java, operations))
            .next()
            .map { doc -> (doc.getInteger("count") ?: 0).toLong() }
            .defaultIfEmpty(0L)
    }

    private fun buildFilterStages(filter: ProblemFilter, userId: String?): MutableList<AggregationOperation> {
        val operations = mutableListOf<AggregationOperation>()
        if (!filter.prefix.isNullOrBlank()) {
            operations += Aggregation.match(Criteria.where("_id").regex("^${filter.prefix}"))
        }
        if (filter.isHard != null) {
            operations += Aggregation.match(Criteria.where("isHard").`is`(filter.isHard))
        }
        if (filter.hasPictures != null) {
            operations += Aggregation.match(Criteria.where("images.0").exists(filter.hasPictures))
        }
        if (filter.hasResult != null) {
            operations += Aggregation.match(Criteria.where("result").exists(filter.hasResult))
        }
        if (filter.status != null && userId != null) {
            operations += lookupProblemStatus(userId)
            operations += Aggregation.match(statusCriteria(filter.status))
        }
        return operations
    }

    private fun lookupProblemStatus(userId: String): AggregationOperation = AggregationOperation { _ ->
        Document(
            $$"$lookup",
            Document()
                .append("from", "problem_status")
                .append("let", Document("pid", $$"$_id"))
                .append(
                    "pipeline",
                    listOf(
                        Document(
                            $$"$match",
                            Document(
                                $$"$expr",
                                Document(
                                    $$"$and",
                                    listOf(
                                        Document($$"$eq", listOf($$"$problemId", $$$"$$pid")),
                                        Document($$"$eq", listOf($$"$userId", userId)),
                                    ),
                                ),
                            ),
                        )
                    ),
                )
                .append("as", "ps"),
        )
    }

    private fun statusCriteria(status: Problem.Status): Criteria =
        when (status) {
            Problem.Status.SOLVED -> Criteria.where("ps.bestStatus").`is`(SubmissionStatus.SUCCESS.name)
            Problem.Status.FAILED -> Criteria.where("ps.bestStatus").`is`(SubmissionStatus.FAILED.name)
            Problem.Status.SOLVING -> Criteria.where("ps.bestStatus").`is`(SubmissionStatus.PENDING.name)
            Problem.Status.NOT_SOLVED -> Criteria.where("ps").size(0)
        }

    private fun withPrefixFilter(prefix: String?, pageRequest: PageRequest): Flux<Problem> =
        if (prefix.isNullOrBlank()) {
            problemRepository.findAll(pageRequest.withSort(semVerSort()))
        } else {
            problemRepository.findProblemsByIdStartingWith(prefix, pageRequest.withSort(semVerSort()))
        }

    private fun countWithPrefixFilter(prefix: String?): Mono<Long> =
        if (prefix.isNullOrBlank()) problemRepository.count() else problemRepository.countByIdStartingWith(prefix)
}
