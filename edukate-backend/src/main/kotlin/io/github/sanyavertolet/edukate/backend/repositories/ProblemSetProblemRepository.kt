package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.ProblemSetProblem
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ProblemSetProblemRepository : ReactiveCrudRepository<ProblemSetProblem, Long> {
    fun findByProblemSetIdOrderByPosition(problemSetId: Long): Flux<ProblemSetProblem>

    @Modifying
    @Query("DELETE FROM problem_set_problems WHERE problem_set_id = :problemSetId")
    fun deleteByProblemSetId(problemSetId: Long): Mono<Void>
}
