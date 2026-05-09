package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.ProblemLocalization
import io.github.sanyavertolet.edukate.common.ContentLanguage
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ProblemLocalizationRepository : ReactiveCrudRepository<ProblemLocalization, Long> {
    fun findByProblemIdAndLanguage(problemId: Long, language: ContentLanguage): Mono<ProblemLocalization>

    fun findFirstByProblemId(problemId: Long): Mono<ProblemLocalization>
}
