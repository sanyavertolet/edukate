package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.dtos.Result
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.storage.keys.ResultFileKey
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Service
/*
 * todo: refactor Result entity in order to split the persistent result with human readable result (with correct pics)
 */
class ResultService(private val problemRepository: ProblemRepository, private val fileManager: FileManager) {
    fun updateResult(result: Result): Mono<String> =
        problemRepository
            .findById(result.id)
            .map { problem -> problem.copy(result = result) }
            .flatMap { problemRepository.save(it) }
            .map { it.id }

    fun updateResultBatch(results: Flux<Result>): Mono<Long> =
        results
            .flatMap { result ->
                problemRepository
                    .findById(result.id)
                    .map { problem -> problem.copy(result = result) }
                    .flatMap { problemRepository.save(it) }
            }
            .count()

    fun findResultById(id: String): Mono<Result> =
        problemRepository.findById(id).flatMap { it.result.toMono() }.flatMap { updateImagesInResult(it) }

    private fun getResultImageList(result: Result): Mono<List<String>> =
        result.images
            .toFlux()
            .map { fileName -> ResultFileKey(result.id, fileName) }
            .flatMap { fileManager.getPresignedUrl(it) }
            .collectList()

    private fun updateImagesInResult(result: Result): Mono<Result> =
        getResultImageList(result).defaultIfEmpty(emptyList()).map { result.copy(images = it) }
}
