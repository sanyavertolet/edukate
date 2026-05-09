package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.configs.LanguageWebFilter
import io.github.sanyavertolet.edukate.backend.dtos.AnswerDto
import io.github.sanyavertolet.edukate.backend.entities.Answer
import io.github.sanyavertolet.edukate.backend.entities.AnswerLocalization
import io.github.sanyavertolet.edukate.backend.repositories.AnswerLocalizationRepository
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.ContentLanguage
import io.github.sanyavertolet.edukate.storage.keys.AnswerFileKey
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Component
class AnswerMapper(
    private val answerLocalizationRepository: AnswerLocalizationRepository,
    private val fileManager: FileManager,
) {
    fun toDto(answer: Answer, problemKey: String): Mono<AnswerDto> {
        val answerId = requireNotNull(answer.id)
        val (bookSlug, problemCode) = problemKey.split("/", limit = 2)
        return Mono.deferContextual { ctx ->
            val language = LanguageWebFilter.fromContext(ctx)
            Mono.zip(
                    resolveLocalization(answerId, language),
                    imageUrls(answer, bookSlug, problemCode).defaultIfEmpty(emptyList()),
                )
                .map { tuple ->
                    val loc = tuple.t1
                    AnswerDto(text = loc.text, notes = loc.notes, images = tuple.t2, language = loc.language)
                }
        }
    }

    private fun resolveLocalization(answerId: Long, language: ContentLanguage): Mono<AnswerLocalization> =
        answerLocalizationRepository
            .findByAnswerIdAndLanguage(answerId, language)
            .switchIfEmpty(Mono.defer { answerLocalizationRepository.findFirstByAnswerId(answerId) })

    private fun imageUrls(answer: Answer, bookSlug: String, problemCode: String): Mono<List<String>> =
        answer.images
            .toFlux()
            .map { fileName -> AnswerFileKey(bookSlug, problemCode, fileName) }
            .flatMap { fileManager.getPresignedUrl(it) }
            .collectList()
}
