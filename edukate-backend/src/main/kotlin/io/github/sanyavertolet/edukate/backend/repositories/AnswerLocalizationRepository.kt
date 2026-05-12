package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.AnswerLocalization
import io.github.sanyavertolet.edukate.common.ContentLanguage
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface AnswerLocalizationRepository : ReactiveCrudRepository<AnswerLocalization, Long> {
    fun findByAnswerIdAndLanguage(answerId: Long, language: ContentLanguage): Mono<AnswerLocalization>

    fun findFirstByAnswerId(answerId: Long): Mono<AnswerLocalization>

    @Modifying
    @Query(
        """
        INSERT INTO answer_localizations (answer_id, language, text, notes)
        VALUES (:answerId, :language, :text, :notes)
        ON CONFLICT (answer_id, language) DO UPDATE SET
            text = EXCLUDED.text,
            notes = EXCLUDED.notes
    """
    )
    fun upsert(answerId: Long, language: ContentLanguage, text: String, notes: String?): Mono<Int>
}
