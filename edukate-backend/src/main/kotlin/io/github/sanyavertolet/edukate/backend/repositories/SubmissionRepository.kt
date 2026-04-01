package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface SubmissionRepository : ReactiveMongoRepository<Submission, String> {
    fun findAllByProblemIdAndUserId(problemId: String, userId: String, pageable: Pageable): Flux<Submission>

    fun findAllByStatusIn(statuses: Collection<SubmissionStatus>, pageable: Pageable): Flux<Submission>

    fun findAllByUserId(userId: String, pageable: Pageable): Flux<Submission>
}
