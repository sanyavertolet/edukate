package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.Answer
import io.github.sanyavertolet.edukate.backend.repositories.AnswerRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AnswerService(private val answerRepository: AnswerRepository, private val problemRepository: ProblemRepository) {
    @Caching(
        evict =
            [
                CacheEvict(cacheNames = ["problems-by-id"], allEntries = true),
                CacheEvict(cacheNames = ["problems-by-key"], allEntries = true),
            ]
    )
    fun saveAnswer(answer: Answer): Mono<Answer> = answerRepository.save(answer)

    @Caching(
        evict =
            [
                CacheEvict(cacheNames = ["problems-by-id"], allEntries = true),
                CacheEvict(cacheNames = ["problems-by-key"], allEntries = true),
            ]
    )
    fun saveAnswerBatch(answers: Flux<Answer>): Mono<Long> = answerRepository.saveAll(answers).count()

    fun findByProblemKey(problemKey: String): Mono<Answer> =
        problemRepository.findByKey(problemKey).flatMap { problem ->
            answerRepository.findByProblemId(requireNotNull(problem.id))
        }

    fun hasAnswer(problemId: Long): Mono<Boolean> = answerRepository.findByProblemId(problemId).hasElement()
}
