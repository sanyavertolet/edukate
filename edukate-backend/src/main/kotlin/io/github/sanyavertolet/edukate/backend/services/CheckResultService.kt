package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.CheckResult
import io.github.sanyavertolet.edukate.backend.repositories.CheckResultRepository
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CheckResultService(
    private val checkResultRepository: CheckResultRepository,
    private val meterRegistry: MeterRegistry,
) {
    /** Direct insert — used for self-check. The DB trigger handles submission status update. */
    fun saveCheckResult(checkResult: CheckResult): Mono<CheckResult> =
        checkResultRepository.save(checkResult).doOnNext {
            meterRegistry.counter("check.outcomes", "status", it.status.name).increment()
        }

    /**
     * Finds the stub by its ID and updates it in-place with the checker result. The DB trigger handles submission status +
     * problem_progress update automatically.
     */
    fun updateFromMessage(message: CheckResultMessage): Mono<CheckResult> =
        checkResultRepository
            .findById(message.checkResultId)
            .orNotFound("CheckResult ${message.checkResultId} not found")
            .map { stub ->
                stub.copy(
                    status = message.status,
                    trustLevel = message.trustLevel,
                    errorType = message.errorType,
                    explanation = message.explanation,
                )
            }
            .flatMap { checkResultRepository.save(it) }
            .doOnNext { meterRegistry.counter("check.outcomes", "status", it.status.name).increment() }

    fun findById(id: Long): Mono<CheckResult> = checkResultRepository.findById(id)

    fun findAllBySubmissionId(submissionId: Long): Flux<CheckResult> =
        checkResultRepository.findBySubmissionId(submissionId, Sort.by(Sort.Direction.DESC, "createdAt"))
}
