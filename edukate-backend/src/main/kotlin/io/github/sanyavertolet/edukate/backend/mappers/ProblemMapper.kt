package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.services.ProblemStatusDecisionManager
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Component
class ProblemMapper(
    private val problemStatusDecisionManager: ProblemStatusDecisionManager,
    private val fileManager: FileManager,
) {
    fun toDto(problem: Problem, authentication: Authentication?): Mono<ProblemDto> =
        problemStatusDecisionManager
            .getStatus(problem.id, authentication)
            .zipWith(imageUrls(problem).collectList(), problem::toProblemDto)

    fun toMetadata(problem: Problem, authentication: Authentication?): Mono<ProblemMetadata> =
        problemStatusDecisionManager.getStatus(problem.id, authentication).map { problem.toProblemMetadata(it) }

    private fun imageUrls(problem: Problem): Flux<String> =
        problem.images.toFlux().map { ProblemFileKey(problem.id, it) }.flatMap { fileManager.getPresignedUrl(it) }
}
