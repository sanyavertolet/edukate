package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface SubmissionRepository : ReactiveCrudRepository<Submission, Long> {
    fun findAllByProblemIdAndUserId(problemId: Long, userId: Long, pageable: Pageable): Flux<Submission>

    fun findAllByStatusIn(statuses: Collection<SubmissionStatus>, pageable: Pageable): Flux<Submission>

    fun findAllByUserId(userId: Long, pageable: Pageable): Flux<Submission>
}
