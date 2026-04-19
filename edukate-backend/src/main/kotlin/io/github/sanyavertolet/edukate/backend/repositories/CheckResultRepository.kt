package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.CheckResult
import org.springframework.data.domain.Sort
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface CheckResultRepository : ReactiveCrudRepository<CheckResult, Long> {
    fun findBySubmissionId(submissionId: Long, sort: Sort): Flux<CheckResult>
}
