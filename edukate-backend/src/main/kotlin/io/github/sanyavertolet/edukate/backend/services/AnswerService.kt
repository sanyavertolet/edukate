package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.dtos.AnswerDto
import io.github.sanyavertolet.edukate.backend.entities.Answer
import io.github.sanyavertolet.edukate.backend.repositories.AnswerRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.storage.keys.ResultFileKey
import org.springframework.cache.annotation.CacheEvict
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
    @CacheEvict(cacheNames = ["problems"], allEntries = true)
    fun saveAnswer(answer: Answer): Mono<Answer> = answerRepository.save(answer)

    @CacheEvict(cacheNames = ["problems"], allEntries = true)
    fun saveAnswerBatch(answers: Flux<Answer>): Mono<Long> = answerRepository.saveAll(answers).count()

    fun findByProblemKey(problemKey: String): Mono<AnswerDto> =
        problemRepository
            .findByKey(problemKey)
            .flatMap { problem -> answerRepository.findByProblemId(requireNotNull(problem.id)) }
            .flatMap { answer -> toDto(answer) }

    fun hasAnswer(problemId: Long): Mono<Boolean> = answerRepository.findByProblemId(problemId).hasElement()

    private fun toDto(answer: Answer): Mono<AnswerDto> =
        getAnswerImageUrls(answer).defaultIfEmpty(emptyList()).map { imageUrls ->
            AnswerDto(answer.text, answer.notes, imageUrls)
        }

    private fun getAnswerImageUrls(answer: Answer): Mono<List<String>> =
        answer.images
            .toFlux()
            .map { fileName -> ResultFileKey(answer.problemId, fileName) }
            .flatMap { fileManager.getPresignedUrl(it) }
            .collectList()
}
