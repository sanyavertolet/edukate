package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.Problem
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.data.repository.reactive.ReactiveSortingRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Suppress("TooManyFunctions")
@Repository
interface ProblemRepository : ReactiveCrudRepository<Problem, Long>, ReactiveSortingRepository<Problem, Long> {
    fun findByKey(key: String): Mono<Problem>

    fun findByKeyIn(keys: Collection<String>): Flux<Problem>

    fun findByIdIn(ids: Collection<Long>): Flux<Problem>

    @Query("SELECT * FROM problems WHERE code LIKE :prefix || '%' ORDER BY string_to_array(code, '.')::int[] LIMIT :limit")
    fun findByCodeStartingWith(prefix: String, limit: Int): Flux<Problem>

    @Query("SELECT * FROM problems ORDER BY RANDOM() LIMIT 1") fun findRandomProblem(): Mono<Problem>

    @Query(
        """
        SELECT p.* FROM problems p
        WHERE NOT EXISTS (
            SELECT 1 FROM problem_progress pp
            WHERE pp.problem_id = p.id AND pp.user_id = :userId
        )
        ORDER BY RANDOM() LIMIT 1
        """
    )
    fun findRandomUnsolvedProblem(userId: Long): Mono<Problem>

    @Query(
        """
        SELECT p.* FROM problems p
        WHERE (:bookId IS NULL OR p.book_id = :bookId)
          AND (:prefix IS NULL OR p.code LIKE :prefix || '%')
          AND (:isHard IS NULL OR p.is_hard = :isHard)
          AND (:hasPictures IS NULL OR (jsonb_array_length(p.images) > 0) = :hasPictures)
          AND (:hasResult IS NULL OR (EXISTS (SELECT 1 FROM answers a WHERE a.problem_id = p.id)) = :hasResult)
          AND (:notSolved = false OR NOT EXISTS (SELECT 1 FROM problem_progress pp WHERE pp.problem_id = p.id AND pp.user_id = :userId))
          AND (:bestStatus IS NULL OR EXISTS (SELECT 1 FROM problem_progress pp WHERE pp.problem_id = p.id AND pp.user_id = :userId AND pp.best_status = :bestStatus))
        ORDER BY string_to_array(p.code, '.')::int[]
        LIMIT :limit OFFSET :offset
        """
    )
    fun findWithFilter(
        bookId: Long?,
        prefix: String?,
        isHard: Boolean?,
        hasPictures: Boolean?,
        hasResult: Boolean?,
        notSolved: Boolean,
        bestStatus: String?,
        userId: Long?,
        limit: Int,
        offset: Long,
    ): Flux<Problem>

    @Query(
        """
        SELECT COUNT(*) FROM problems p
        WHERE (:bookId IS NULL OR p.book_id = :bookId)
          AND (:prefix IS NULL OR p.code LIKE :prefix || '%')
          AND (:isHard IS NULL OR p.is_hard = :isHard)
          AND (:hasPictures IS NULL OR (jsonb_array_length(p.images) > 0) = :hasPictures)
          AND (:hasResult IS NULL OR (EXISTS (SELECT 1 FROM answers a WHERE a.problem_id = p.id)) = :hasResult)
          AND (:notSolved = false OR NOT EXISTS (SELECT 1 FROM problem_progress pp WHERE pp.problem_id = p.id AND pp.user_id = :userId))
          AND (:bestStatus IS NULL OR EXISTS (SELECT 1 FROM problem_progress pp WHERE pp.problem_id = p.id AND pp.user_id = :userId AND pp.best_status = :bestStatus))
        """
    )
    fun countWithFilter(
        bookId: Long?,
        prefix: String?,
        isHard: Boolean?,
        hasPictures: Boolean?,
        hasResult: Boolean?,
        notSolved: Boolean,
        bestStatus: String?,
        userId: Long?,
    ): Mono<Long>
}
