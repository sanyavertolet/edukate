package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface SubmissionRepository : ReactiveCrudRepository<Submission, Long> {
    fun findAllByProblemIdAndUserId(problemId: Long, userId: Long, pageable: Pageable): Flux<Submission>

    fun findAllByStatusIn(statuses: Collection<SubmissionStatus>, pageable: Pageable): Flux<Submission>

    fun findAllByUserId(userId: Long, pageable: Pageable): Flux<Submission>

    @Query(
        """
        SELECT s.* FROM submissions s
        JOIN users u ON s.user_id = u.id
        JOIN problems p ON s.problem_id = p.id
        JOIN books b ON p.book_id = b.id
        WHERE (:userPrefix IS NULL OR u.name LIKE :userPrefix || '%')
          AND (:bookSlugPrefix IS NULL OR b.slug LIKE :bookSlugPrefix || '%')
          AND (:problemCodePrefix IS NULL OR p.code LIKE :problemCodePrefix || '%')
          AND (:status IS NULL OR s.status = :status)
        ORDER BY s.updated_at DESC, s.id DESC
        LIMIT :limit OFFSET :offset
    """
    )
    fun findWithFilter(
        userPrefix: String?,
        bookSlugPrefix: String?,
        problemCodePrefix: String?,
        status: String?,
        limit: Int,
        offset: Long,
    ): Flux<Submission>

    @Query(
        """
        SELECT COUNT(*) FROM submissions s
        JOIN users u ON s.user_id = u.id
        JOIN problems p ON s.problem_id = p.id
        JOIN books b ON p.book_id = b.id
        WHERE (:userPrefix IS NULL OR u.name LIKE :userPrefix || '%')
          AND (:bookSlugPrefix IS NULL OR b.slug LIKE :bookSlugPrefix || '%')
          AND (:problemCodePrefix IS NULL OR p.code LIKE :problemCodePrefix || '%')
          AND (:status IS NULL OR s.status = :status)
    """
    )
    fun countWithFilter(
        userPrefix: String?,
        bookSlugPrefix: String?,
        problemCodePrefix: String?,
        status: String?,
    ): Mono<Long>
}
