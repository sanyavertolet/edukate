@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.files.FileObject
import io.github.sanyavertolet.edukate.backend.entities.files.FileObjectMetadata
import io.github.sanyavertolet.edukate.backend.permissions.SubmissionPermissionEvaluator
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository
import io.github.sanyavertolet.edukate.backend.repositories.SubmissionRepository
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.backend.services.files.SubmissionFileService
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
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
    private val fileManager: FileManager = mockk()
    private val submissionFileService: SubmissionFileService = mockk()
    private val userService: UserService = mockk()
    private val fileObjectRepository: FileObjectRepository = mockk()
    private val problemService: ProblemService = mockk()
    private val submissionPermissionEvaluator: SubmissionPermissionEvaluator = mockk()
    private val meterRegistry = SimpleMeterRegistry()
    private lateinit var service: SubmissionService

    @BeforeEach
    fun setUp() {
        service =
            SubmissionService(
                submissionRepository,
                fileManager,
                submissionFileService,
                userService,
                fileObjectRepository,
                problemService,
                submissionPermissionEvaluator,
                meterRegistry,
            )
    }

    // region saveSubmission

    @Test
    fun `saveSubmission creates entity and moves files`() {
        val request = BackendFixtures.createSubmissionRequest(problemId = "1.0.0", fileNames = listOf("solution.txt"))
        val savedSubmission = BackendFixtures.submission(id = "sub-new", problemId = "1.0.0", userId = "user-1")
        val fileKey = SubmissionFileKey("user-1", "1.0.0", "sub-new", "solution.txt")
        val fileObject =
            FileObject(
                id = "fo-1",
                keyPath = fileKey.toString(),
                key = fileKey,
                type = "submission",
                ownerUserId = "user-1",
                metadata = FileObjectMetadata(Instant.now(), 100L, "text/plain"),
            )

        every { submissionRepository.save(match { it.id == null && it.problemId == "1.0.0" }) } returns
            Mono.just(savedSubmission)
        every { submissionFileService.moveSubmissionFiles("user-1", "sub-new", request) } returns Flux.empty()
        every { fileObjectRepository.findByKeyPath(fileKey.toString()) } returns Mono.just(fileObject)
        every { submissionRepository.save(match { it.id == "sub-new" && it.fileObjectIds == listOf("fo-1") }) } returns
            Mono.just(savedSubmission.withFileObjectIds(listOf("fo-1")))

        StepVerifier.create(service.saveSubmission("user-1", request))
            .assertNext { sub -> assertThat(sub.fileObjectIds).containsExactly("fo-1") }
            .verifyComplete()
    }

    @Test
    fun `saveSubmission with auth extraction delegates`() {
        val request = BackendFixtures.createSubmissionRequest()
        val savedSubmission = BackendFixtures.submission(id = "sub-1", problemId = "1.0.0", userId = "user-1")
        val auth = BackendFixtures.mockAuthentication(userId = "user-1")

        every { submissionRepository.save(match { it.id == null }) } returns Mono.just(savedSubmission)
        every { submissionFileService.moveSubmissionFiles("user-1", "sub-1", request) } returns Flux.empty()
        every { fileObjectRepository.findByKeyPath(any()) } returns Mono.empty()
        every { submissionRepository.save(match { it.id == "sub-1" }) } returns
            Mono.just(savedSubmission.withFileObjectIds(emptyList()))

        StepVerifier.create(service.saveSubmission(request, auth)).expectNextCount(1).verifyComplete()
    }

    // endregion

    // region update

    @Test
    fun `update delegates to repository`() {
        val submission = BackendFixtures.submission()
        every { submissionRepository.save(submission) } returns Mono.just(submission)

        StepVerifier.create(service.update(submission)).expectNext(submission).verifyComplete()

        verify(exactly = 1) { submissionRepository.save(submission) }
    }

    // endregion

    // region findSubmissionsByProblemIdAndUserId / findUserSubmissions / findSubmissionsByStatusIn

    @Test
    fun findSubmissionsByProblemIdAndUserId() {
        val submission = BackendFixtures.submission()
        every { submissionRepository.findAllByProblemIdAndUserId("1.0.0", "user-1", Pageable.unpaged()) } returns
            Flux.just(submission)

        StepVerifier.create(service.findSubmissionsByProblemIdAndUserId("1.0.0", "user-1", Pageable.unpaged()))
            .expectNext(submission)
            .verifyComplete()
    }

    @Test
    fun `findUserSubmissions with Problem id`() {
        val submission = BackendFixtures.submission()
        every { submissionRepository.findAllByProblemIdAndUserId("1.0.0", "user-1", Pageable.unpaged()) } returns
            Flux.just(submission)

        StepVerifier.create(service.findUserSubmissions("user-1", "1.0.0", Pageable.unpaged()))
            .expectNext(submission)
            .verifyComplete()
    }

    @Test
    fun `findUserSubmissions without Problem id`() {
        val submission = BackendFixtures.submission()
        every { submissionRepository.findAllByUserId("user-1", Pageable.unpaged()) } returns Flux.just(submission)

        StepVerifier.create(service.findUserSubmissions("user-1", null, Pageable.unpaged()))
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
        val submission = BackendFixtures.submission(id = "sub-1", userId = "user-1")
        every { submissionRepository.findById("sub-1") } returns Mono.just(submission)
        every { submissionPermissionEvaluator.isOwner(submission, "user-1") } returns true

        StepVerifier.create(service.getSubmissionIfOwns("sub-1", "user-1")).expectNext(submission).verifyComplete()
    }

    @Test
    fun `getSubmissionIfOwns not found`() {
        every { submissionRepository.findById("nonexistent") } returns Mono.empty()

        StepVerifier.create(service.getSubmissionIfOwns("nonexistent", "user-1"))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.NOT_FOUND }
            .verify()
    }

    @Test
    fun `getSubmissionIfOwns not owner`() {
        val submission = BackendFixtures.submission(id = "sub-1", userId = "user-1")
        every { submissionRepository.findById("sub-1") } returns Mono.just(submission)
        every { submissionPermissionEvaluator.isOwner(submission, "other-user") } returns false

        StepVerifier.create(service.getSubmissionIfOwns("sub-1", "other-user"))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.FORBIDDEN }
            .verify()
    }

    // endregion

    // region prepareDto

    @Test
    fun `prepareDto collects file urls and user name`() {
        val fileKey = SubmissionFileKey("user-1", "1.0.0", "sub-1", "solution.txt")
        val fileObject =
            FileObject(
                id = "fo-1",
                keyPath = fileKey.toString(),
                key = fileKey,
                type = "submission",
                ownerUserId = "user-1",
                metadata = FileObjectMetadata(Instant.now(), 100L, "text/plain"),
            )
        val submission = BackendFixtures.submission(id = "sub-1", userId = "user-1", fileObjectIds = listOf("fo-1"))

        every { fileObjectRepository.findAllById(listOf("fo-1")) } returns Flux.just(fileObject)
        every { fileManager.getPresignedUrl(fileKey) } returns Mono.just("https://s3/solution.txt")
        every { userService.findUserName("user-1") } returns Mono.just("alice")

        StepVerifier.create(service.prepareDto(submission))
            .assertNext { dto ->
                assertThat(dto.userName).isEqualTo("alice")
                assertThat(dto.fileUrls).containsExactly("https://s3/solution.txt")
            }
            .verifyComplete()
    }

    // endregion

    // region prepareContext

    @Test
    fun `prepareContext builds SubmissionContext`() {
        val problem = BackendFixtures.problem(id = "1.0.0", text = "Solve it", images = listOf("img.png"))
        val fileKey = SubmissionFileKey("user-1", "1.0.0", "sub-1", "solution.txt")
        val fileObject =
            FileObject(
                id = "fo-1",
                keyPath = fileKey.toString(),
                key = fileKey,
                type = "submission",
                ownerUserId = "user-1",
                metadata = FileObjectMetadata(Instant.now(), 100L, "text/plain"),
            )
        val submission = BackendFixtures.submission(id = "sub-1", userId = "user-1", fileObjectIds = listOf("fo-1"))

        every { problemService.findProblemById("1.0.0") } returns Mono.just(problem)
        every { fileManager.getFileObjectsByIds(listOf("fo-1")) } returns Flux.just(fileObject)

        StepVerifier.create(service.prepareContext(submission))
            .assertNext { ctx ->
                assertThat(ctx.submissionId).isEqualTo("sub-1")
                assertThat(ctx.problemId).isEqualTo("1.0.0")
                assertThat(ctx.problemText).isEqualTo("Solve it")
                assertThat(ctx.problemImageRawKeys).containsExactly(ProblemFileKey("1.0.0", "img.png").toString())
                assertThat(ctx.submissionImageRawKeys).containsExactly(fileKey.toString())
            }
            .verifyComplete()
    }

    // endregion
}
