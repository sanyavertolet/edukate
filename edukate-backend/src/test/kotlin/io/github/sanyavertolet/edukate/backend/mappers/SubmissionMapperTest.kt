@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.files.FileObject
import io.github.sanyavertolet.edukate.backend.entities.files.FileObjectMetadata
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
import io.github.sanyavertolet.edukate.storage.keys.SubmissionFileKey
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class SubmissionMapperTest {
    private val fileObjectRepository: FileObjectRepository = mockk()
    private val fileManager: FileManager = mockk()
    private val userService: UserService = mockk()
    private val problemService: ProblemService = mockk()
    private lateinit var mapper: SubmissionMapper

    @BeforeEach
    fun setUp() {
        mapper = SubmissionMapper(fileObjectRepository, fileManager, userService, problemService)
    }

    @Test
    fun `toDto collects file urls and user name`() {
        val fileKey = SubmissionFileKey(1L, 1L, 1L, "solution.txt")
        val fileObject =
            FileObject(
                id = 1L,
                keyPath = fileKey.toString(),
                key = fileKey,
                type = "submission",
                ownerUserId = 1L,
                metadata = FileObjectMetadata(Instant.now(), 100L, "text/plain"),
            )
        val submission = BackendFixtures.submission(id = 1L, userId = 1L, fileObjectIds = listOf("1"))

        every { fileObjectRepository.findAllById(listOf(1L)) } returns Flux.just(fileObject)
        every { fileManager.getPresignedUrl(fileKey) } returns Mono.just("https://s3/solution.txt")
        every { userService.findUserById(1L) } returns Mono.just(BackendFixtures.user(id = 1L, name = "alice"))
        every { problemService.findProblemById(1L) } returns Mono.just(BackendFixtures.problem(id = 1L, code = "P1"))

        StepVerifier.create(mapper.toDto(submission))
            .assertNext { dto ->
                assertThat(dto.userName).isEqualTo("alice")
                assertThat(dto.fileUrls).containsExactly("https://s3/solution.txt")
            }
            .verifyComplete()
    }

    @Test
    fun `prepareContext builds SubmissionContext`() {
        val problem = BackendFixtures.problem(id = 1L, code = "1.1.1", text = "Solve it", images = listOf("img.png"))
        val fileKey = SubmissionFileKey(1L, 1L, 1L, "solution.txt")
        val fileObject =
            FileObject(
                id = 1L,
                keyPath = fileKey.toString(),
                key = fileKey,
                type = "submission",
                ownerUserId = 1L,
                metadata = FileObjectMetadata(Instant.now(), 100L, "text/plain"),
            )
        val submission = BackendFixtures.submission(id = 1L, userId = 1L, fileObjectIds = listOf("1"))

        every { problemService.findProblemById(1L) } returns Mono.just(problem)
        every { fileObjectRepository.findAllById(listOf(1L)) } returns Flux.just(fileObject)

        StepVerifier.create(mapper.prepareContext(submission))
            .assertNext { ctx ->
                assertThat(ctx.submissionId).isEqualTo(1L)
                assertThat(ctx.problemId).isEqualTo(1L)
                assertThat(ctx.problemText).isEqualTo("Solve it")
                assertThat(ctx.problemImageRawKeys).containsExactly(ProblemFileKey(1L, "img.png").toString())
                assertThat(ctx.submissionImageRawKeys).containsExactly(fileKey.toString())
            }
            .verifyComplete()
    }
}
