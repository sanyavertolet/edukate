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
        WHERE ps.user_id_role_map ->> CAST(:userId AS TEXT) = ANY(:roles)
        ORDER BY ps.id
        """
    )
    fun findByUserIdAndRoles(userId: Long, roles: Array<String>, pageable: Pageable): Flux<ProblemSet>

    @Query(
        """
        SELECT ps.* FROM problem_sets ps
        WHERE ps.user_id_role_map -> CAST(:userId AS TEXT) IS NOT NULL
          AND (ps.name ILIKE CONCAT('%', :query, '%')
               OR ps.share_code ILIKE CONCAT('%', :query, '%'))
          AND (CAST(:problemKey AS TEXT) IS NULL
               OR EXISTS (
                   SELECT 1 FROM problem_set_problems psp
                   JOIN problems p ON psp.problem_id = p.id
                   WHERE psp.problem_set_id = ps.id
                     AND p.key = :problemKey
               ))
        ORDER BY ps.id
        """
    )
    fun searchByUserIdAndQuery(userId: Long, query: String, problemKey: String?, pageable: Pageable): Flux<ProblemSet>
}
