package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.permissions.SubmissionPermissionEvaluator
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository
import io.github.sanyavertolet.edukate.backend.repositories.SubmissionRepository
import io.github.sanyavertolet.edukate.backend.services.files.SubmissionFileService
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.github.sanyavertolet.edukate.common.utils.monoId
import io.github.sanyavertolet.edukate.storage.keys.SubmissionFileKey
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val submissionFileService: SubmissionFileService,
    private val fileObjectRepository: FileObjectRepository,
    private val submissionPermissionEvaluator: SubmissionPermissionEvaluator,
    private val meterRegistry: MeterRegistry,
) {
    @Transactional
    fun saveSubmission(submissionRequest: CreateSubmissionRequest, authentication: Authentication): Mono<Submission> =
        authentication.monoId().flatMap { userId -> saveSubmission(userId, submissionRequest) }

    fun update(submission: Submission): Mono<Submission> = submissionRepository.save(submission)

    @Transactional
    fun saveSubmission(userId: String, submissionRequest: CreateSubmissionRequest): Mono<Submission> =
        submissionRepository.save(Submission.of(submissionRequest.problemId, userId)).flatMap { submission ->
            submissionFileService
                .moveSubmissionFiles(userId, requireNotNull(submission.id), submissionRequest)
                .then(
                    submissionRequest.fileNames
                        .toFlux()
                        .map { fileName ->
                            SubmissionFileKey(userId, submissionRequest.problemId, submission.id, fileName).toString()
                        }
                        .flatMap { fileObjectRepository.findByKeyPath(it) }
                        .map { requireNotNull(it.id) }
                        .collectList()
                )
                .flatMap { ids -> submissionRepository.save(submission.withFileObjectIds(ids)) }
                .doOnNext {
                    meterRegistry.counter("submissions.created", "problemId", submissionRequest.problemId).increment()
                }
        }

    fun findById(id: String): Mono<Submission> = submissionRepository.findById(id)

    fun findSubmissionsByProblemIdAndUserId(problemId: String, userId: String, pageable: Pageable): Flux<Submission> =
        submissionRepository.findAllByProblemIdAndUserId(problemId, userId, pageable)

    /** If problemId is null, then returns submissions by user id regardless of the problem. */
    fun findUserSubmissions(userId: String, problemId: String?, pageable: Pageable): Flux<Submission> =
        problemId?.let { submissionRepository.findAllByProblemIdAndUserId(it, userId, pageable) }
            ?: submissionRepository.findAllByUserId(userId, pageable)

    fun findSubmissionsByStatusIn(statuses: List<SubmissionStatus>, pageable: Pageable): Flux<Submission> =
        submissionRepository.findAllByStatusIn(statuses, pageable)

    fun getSubmissionIfOwns(submissionId: String, userId: String): Mono<Submission> =
        submissionRepository
            .findById(submissionId)
            .switchIfEmpty(ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found").toMono())
            .filter { submission -> submissionPermissionEvaluator.isOwner(submission, userId) }
            .switchIfEmpty(ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied").toMono())
}
