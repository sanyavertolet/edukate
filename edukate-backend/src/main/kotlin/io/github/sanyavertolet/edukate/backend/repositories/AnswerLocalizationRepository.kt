package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.AnswerLocalization
import io.github.sanyavertolet.edukate.common.ContentLanguage
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface AnswerLocalizationRepository : ReactiveCrudRepository<AnswerLocalization, Long> {
    fun findByAnswerIdAndLanguage(answerId: Long, language: ContentLanguage): Mono<AnswerLocalization>

    fun findFirstByAnswerId(answerId: Long): Mono<AnswerLocalization>
}
