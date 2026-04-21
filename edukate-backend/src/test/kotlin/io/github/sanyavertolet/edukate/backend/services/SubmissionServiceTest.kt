@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.files.FileObject
import io.github.sanyavertolet.edukate.backend.entities.files.FileObjectMetadata
import io.github.sanyavertolet.edukate.backend.permissions.SubmissionPermissionEvaluator
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.SubmissionRepository
import io.github.sanyavertolet.edukate.backend.services.files.SubmissionFileService
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.github.sanyavertolet.edukate.storage.keys.SubmissionFileKey
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class SubmissionServiceTest {
    private val submissionRepository: SubmissionRepository = mockk()
    private val problemRepository: ProblemRepository = mockk()
    private val submissionFileService: SubmissionFileService = mockk()
    private val fileObjectRepository: FileObjectRepository = mockk()
    private val submissionPermissionEvaluator: SubmissionPermissionEvaluator = mockk()
    private val problemProgressService: ProblemProgressService = mockk()
    private val meterRegistry = SimpleMeterRegistry()
    private lateinit var service: SubmissionService

    @BeforeEach
    fun setUp() {
        service =
            SubmissionService(
                submissionRepository,
                problemRepository,
                submissionFileService,
                fileObjectRepository,
                submissionPermissionEvaluator,
                problemProgressService,
                meterRegistry,
            )
    }

    // region saveSubmission

    @Test
    fun `saveSubmission creates entity and moves files`() {
        val request =
            BackendFixtures.createSubmissionRequest(problemKey = "savchenko/P1", fileNames = listOf("solution.txt"))
        val problem = BackendFixtures.problem(id = 1L, code = "P1")
        val savedSubmission = BackendFixtures.submission(id = 10L, problemId = 1L, userId = 1L)
        val fileKey = SubmissionFileKey(1L, 1L, 10L, "solution.txt")
        val fileObject =
            FileObject(
                id = 1L,
                keyPath = fileKey.toString(),
                key = fileKey,
                type = "submission",
                ownerUserId = 1L,
                metadata = FileObjectMetadata(Instant.now(), 100L, "text/plain"),
            )

        every { problemRepository.findByKey("savchenko/P1") } returns Mono.just(problem)
        every { submissionRepository.save(match { it.id == null && it.problemId == 1L }) } returns Mono.just(savedSubmission)
        every { submissionFileService.moveSubmissionFiles(1L, 10L, 1L, request) } returns Flux.empty()
        every { fileObjectRepository.findByKeyPath(fileKey.toString()) } returns Mono.just(fileObject)
        every { submissionRepository.save(match { it.id == 10L && it.fileObjectIds == listOf("1") }) } returns
            Mono.just(savedSubmission.withFileObjectIds(listOf("1")))
        every { problemProgressService.updateProgress(any()) } returns Mono.empty()

        StepVerifier.create(service.saveSubmission(1L, request))
            .assertNext { sub -> assertThat(sub.fileObjectIds).containsExactly("1") }
            .verifyComplete()
    }

    @Test
    fun `saveSubmission with auth extraction delegates`() {
        val request = BackendFixtures.createSubmissionRequest()
        val problem = BackendFixtures.problem(id = 1L, code = "P1")
        val savedSubmission = BackendFixtures.submission(id = 1L, problemId = 1L, userId = 1L)
        val auth = BackendFixtures.mockAuthentication(userId = 1L)

        every { problemRepository.findByKey("savchenko/P1") } returns Mono.just(problem)
        every { submissionRepository.save(match { it.id == null }) } returns Mono.just(savedSubmission)
        every { submissionFileService.moveSubmissionFiles(1L, 1L, 1L, request) } returns Flux.empty()
        every { fileObjectRepository.findByKeyPath(any()) } returns Mono.empty()
        every { submissionRepository.save(match { it.id == 1L }) } returns
            Mono.just(savedSubmission.withFileObjectIds(emptyList()))
        every { problemProgressService.updateProgress(any()) } returns Mono.empty()

        StepVerifier.create(service.saveSubmission(request, auth)).expectNextCount(1).verifyComplete()
    }

    // endregion

    // region update

    @Test
    fun `update delegates to repository`() {
        val submission = BackendFixtures.submission()
        every { submissionRepository.save(submission) } returns Mono.just(submission)
        every { problemProgressService.updateProgress(submission) } returns Mono.empty()

        StepVerifier.create(service.update(submission)).expectNext(submission).verifyComplete()

        verify(exactly = 1) { submissionRepository.save(submission) }
    }

    // endregion

    // region findSubmissionsByProblemIdAndUserId / findUserSubmissions / findSubmissionsByStatusIn

    @Test
    fun findSubmissionsByProblemIdAndUserId() {
        val submission = BackendFixtures.submission()
        every { submissionRepository.findAllByProblemIdAndUserId(1L, 1L, Pageable.unpaged()) } returns Flux.just(submission)

        StepVerifier.create(service.findSubmissionsByProblemIdAndUserId(1L, 1L, Pageable.unpaged()))
            .expectNext(submission)
            .verifyComplete()
    }

    @Test
    fun `findUserSubmissions with Problem id`() {
        val submission = BackendFixtures.submission()
        every { submissionRepository.findAllByProblemIdAndUserId(1L, 1L, Pageable.unpaged()) } returns Flux.just(submission)

        StepVerifier.create(service.findUserSubmissions(1L, 1L, Pageable.unpaged())).expectNext(submission).verifyComplete()
    }

    @Test
    fun `findUserSubmissions without Problem id`() {
        val submission = BackendFixtures.submission()
        every { submissionRepository.findAllByUserId(1L, Pageable.unpaged()) } returns Flux.just(submission)

        StepVerifier.create(service.findUserSubmissions(1L, null, Pageable.unpaged()))
            .expectNext(submission)
            .verifyComplete()
    }

    @Test
    fun findSubmissionsByStatusIn() {
        val submission = BackendFixtures.submission(status = SubmissionStatus.PENDING)
        val statuses = listOf(SubmissionStatus.PENDING)
        every { submissionRepository.findAllByStatusIn(statuses, Pageable.unpaged()) } returns Flux.just(submission)

        StepVerifier.create(service.findSubmissionsByStatusIn(statuses, Pageable.unpaged()))
            .expectNext(submission)
            .verifyComplete()
    }

    // endregion

    // region getSubmissionIfOwns

    @Test
    fun `getSubmissionIfOwns success`() {
        val submission = BackendFixtures.submission(id = 1L, userId = 1L)
        every { submissionRepository.findById(1L) } returns Mono.just(submission)
        every { submissionPermissionEvaluator.isOwner(submission, 1L) } returns true

        StepVerifier.create(service.getSubmissionIfOwns(1L, 1L)).expectNext(submission).verifyComplete()
    }

    @Test
    fun `getSubmissionIfOwns not found`() {
        every { submissionRepository.findById(999L) } returns Mono.empty()

        StepVerifier.create(service.getSubmissionIfOwns(999L, 1L))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.NOT_FOUND }
            .verify()
    }

    @Test
    fun `getSubmissionIfOwns not owner`() {
        val submission = BackendFixtures.submission(id = 1L, userId = 1L)
        every { submissionRepository.findById(1L) } returns Mono.just(submission)
        every { submissionPermissionEvaluator.isOwner(submission, 2L) } returns false

        StepVerifier.create(service.getSubmissionIfOwns(1L, 2L))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.FORBIDDEN }
            .verify()
    }

    // endregion
}
