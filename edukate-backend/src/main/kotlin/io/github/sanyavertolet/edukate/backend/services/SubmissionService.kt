package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.permissions.SubmissionPermissionEvaluator
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.SubmissionRepository
import io.github.sanyavertolet.edukate.backend.services.files.SubmissionFileService
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.github.sanyavertolet.edukate.common.utils.monoId
import io.github.sanyavertolet.edukate.common.utils.orForbidden
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.github.sanyavertolet.edukate.storage.keys.SubmissionFileKey
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val problemRepository: ProblemRepository,
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
    fun saveSubmission(userId: Long, submissionRequest: CreateSubmissionRequest): Mono<Submission> =
        problemRepository
            .findByKey(submissionRequest.problemKey)
            .orNotFound("Problem not found: ${submissionRequest.problemKey}")
            .flatMap { problem ->
                val problemId = requireNotNull(problem.id)
                submissionRepository.save(Submission.of(problemId, userId)).flatMap { submission ->
                    val submissionId = requireNotNull(submission.id)
                    submissionFileService
                        .moveSubmissionFiles(userId, submissionId, problemId, submissionRequest)
                        .then(
                            submissionRequest.fileNames
                                .toFlux()
                                .map { fileName -> SubmissionFileKey(userId, problemId, submissionId, fileName).toString() }
                                .flatMap { fileObjectRepository.findByKeyPath(it) }
                                .map { requireNotNull(it.id).toString() }
                                .collectList()
                        )
                        .flatMap { ids -> submissionRepository.save(submission.withFileObjectIds(ids)) }
                        .doOnNext {
                            meterRegistry
                                .counter("submissions.created", "problemKey", submissionRequest.problemKey)
                                .increment()
                        }
                }
            }

    fun findById(id: Long): Mono<Submission> = submissionRepository.findById(id)

    fun findSubmissionsByProblemIdAndUserId(problemId: Long, userId: Long, pageable: Pageable): Flux<Submission> =
        submissionRepository.findAllByProblemIdAndUserId(problemId, userId, pageable)

    fun findUserSubmissions(userId: Long, problemId: Long?, pageable: Pageable): Flux<Submission> =
        problemId?.let { submissionRepository.findAllByProblemIdAndUserId(it, userId, pageable) }
            ?: submissionRepository.findAllByUserId(userId, pageable)

    fun findSubmissionsByStatusIn(statuses: List<SubmissionStatus>, pageable: Pageable): Flux<Submission> =
        submissionRepository.findAllByStatusIn(statuses, pageable)

    fun getSubmissionIfOwns(submissionId: Long, userId: Long): Mono<Submission> =
        submissionRepository
            .findById(submissionId)
            .orNotFound("Submission not found")
            .filter { submission -> submissionPermissionEvaluator.isOwner(submission, userId) }
            .orForbidden("Access denied")
}
