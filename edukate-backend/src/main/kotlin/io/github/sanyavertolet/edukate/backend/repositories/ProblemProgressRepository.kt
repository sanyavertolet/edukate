package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.ProblemProgress
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ProblemProgressRepository : ReactiveCrudRepository<ProblemProgress, Long> {
    fun findByUserIdAndProblemId(userId: Long, problemId: Long): Mono<ProblemProgress>

    @Query(
        """
        SELECT COUNT(*) FROM problem_progress pp
        JOIN problem_set_problems psp ON psp.problem_id = pp.problem_id
        WHERE psp.problem_set_id = :problemSetId
          AND pp.user_id = :userId
          AND pp.best_status = 'SUCCESS'
        """
    )
    fun countSolvedInProblemSet(problemSetId: Long, userId: Long): Mono<Long>
}
