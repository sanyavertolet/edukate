package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.dtos.AnswerDto
import io.github.sanyavertolet.edukate.backend.entities.Answer
import io.github.sanyavertolet.edukate.backend.repositories.AnswerRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.storage.keys.AnswerFileKey
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Service
class AnswerService(
    private val answerRepository: AnswerRepository,
    private val problemRepository: ProblemRepository,
    private val fileManager: FileManager,
) {
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

    fun findByProblemKey(problemKey: String): Mono<AnswerDto> =
        problemRepository.findByKey(problemKey).flatMap { problem ->
            answerRepository.findByProblemId(requireNotNull(problem.id)).flatMap { answer -> toDto(answer, problem.key) }
        }

    fun hasAnswer(problemId: Long): Mono<Boolean> = answerRepository.findByProblemId(problemId).hasElement()

    private fun toDto(answer: Answer, problemKey: String): Mono<AnswerDto> {
        val (bookSlug, problemCode) = problemKey.split("/", limit = 2)
        return getAnswerImageUrls(answer, bookSlug, problemCode).defaultIfEmpty(emptyList()).map { imageUrls ->
            AnswerDto(answer.text, answer.notes, imageUrls)
        }
    }

    private fun getAnswerImageUrls(answer: Answer, bookSlug: String, problemCode: String): Mono<List<String>> =
        answer.images
            .toFlux()
            .map { fileName -> AnswerFileKey(bookSlug, problemCode, fileName) }
            .flatMap { fileManager.getPresignedUrl(it) }
            .collectList()
}
