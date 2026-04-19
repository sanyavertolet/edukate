package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.ProblemSet
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ProblemSetRepository : ReactiveCrudRepository<ProblemSet, Long> {
    fun findByIsPublic(isPublic: Boolean, pageable: Pageable): Flux<ProblemSet>

    fun findByShareCode(shareCode: String): Mono<ProblemSet>

    @Query(
        """
        SELECT ps.* FROM problem_sets ps
        WHERE ps.user_id_role_map -> CAST(:userId AS TEXT) IS NOT NULL
        ORDER BY ps.id
        """
    )
    fun findByUserId(userId: Long, pageable: Pageable): Flux<ProblemSet>
}
