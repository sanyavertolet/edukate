package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.CheckResult
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.repositories.CheckResultRepository
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CheckResultService(
    private val checkResultRepository: CheckResultRepository,
    private val submissionService: SubmissionService,
    private val meterRegistry: MeterRegistry,
) {
    @Transactional
    fun saveAndUpdateSubmission(checkResult: CheckResult): Mono<Pair<CheckResult, Submission>> =
        submissionService
            .findById(checkResult.submissionId)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")))
            .flatMap { submission -> checkResultRepository.save(checkResult).map { saved -> Pair(saved, submission) } }
            .flatMap { (saved, submission) ->
                val newStatus = SubmissionStatus.from(saved.status)
                val updatedSubmission = submission.withStatus(SubmissionStatus.best(submission.status, newStatus))
                submissionService
                    .update(updatedSubmission)
                    .doOnNext { meterRegistry.counter("check.outcomes", "status", saved.status.name).increment() }
                    .thenReturn(Pair(saved, submission))
            }

    fun findById(id: String): Mono<CheckResult> = checkResultRepository.findById(id)

    fun findAllBySubmissionId(submissionId: String): Flux<CheckResult> =
        checkResultRepository.findBySubmissionId(submissionId, Sort.by(Sort.Direction.DESC, "createdAt"))
}
