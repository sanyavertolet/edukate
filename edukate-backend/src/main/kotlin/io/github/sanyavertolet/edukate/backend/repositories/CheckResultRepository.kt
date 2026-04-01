package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.CheckResult
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface CheckResultRepository : ReactiveMongoRepository<CheckResult, String> {
    fun findBySubmissionId(submissionId: String, sort: Sort): Flux<CheckResult>
}
