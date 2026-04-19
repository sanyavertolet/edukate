package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.ProblemProgress
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ProblemProgressRepository : ReactiveCrudRepository<ProblemProgress, Long> {
    fun findByUserIdAndProblemId(userId: Long, problemId: Long): Mono<ProblemProgress>
}
