package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.Answer
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface AnswerRepository : ReactiveCrudRepository<Answer, Long> {
    fun findByProblemId(problemId: Long): Mono<Answer>
}
