package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.ProblemLocalization
import io.github.sanyavertolet.edukate.backend.entities.Subproblem
import io.github.sanyavertolet.edukate.common.ContentLanguage
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ProblemLocalizationRepository : ReactiveCrudRepository<ProblemLocalization, Long> {
    fun findByProblemIdAndLanguage(problemId: Long, language: ContentLanguage): Mono<ProblemLocalization>

    fun findFirstByProblemId(problemId: Long): Mono<ProblemLocalization>

    @Modifying
    @Query(
        """
        INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
        VALUES (:problemId, :language, :text, :tags, :subproblems)
        ON CONFLICT (problem_id, language) DO UPDATE SET
            text = EXCLUDED.text,
            tags = EXCLUDED.tags,
            subproblems = EXCLUDED.subproblems
    """
    )
    fun upsert(
        problemId: Long,
        language: ContentLanguage,
        text: String,
        tags: List<String>,
        subproblems: List<Subproblem>,
    ): Mono<Int>
}
