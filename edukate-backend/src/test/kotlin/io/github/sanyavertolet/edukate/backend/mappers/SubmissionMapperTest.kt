@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.AnswerLocalization
import io.github.sanyavertolet.edukate.backend.entities.ProblemLocalization
import io.github.sanyavertolet.edukate.backend.entities.Subproblem
import io.github.sanyavertolet.edukate.backend.entities.files.FileObject
import io.github.sanyavertolet.edukate.backend.entities.files.FileObjectMetadata
import io.github.sanyavertolet.edukate.backend.repositories.AnswerLocalizationRepository
import io.github.sanyavertolet.edukate.backend.repositories.AnswerRepository
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemLocalizationRepository
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.ContentLanguage
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
    private val answerRepository: AnswerRepository = mockk()
    private val problemLocalizationRepository: ProblemLocalizationRepository = mockk()
    private val answerLocalizationRepository: AnswerLocalizationRepository = mockk()
    private lateinit var mapper: SubmissionMapper

    @BeforeEach
    fun setUp() {
        mapper =
            SubmissionMapper(
                fileObjectRepository,
                fileManager,
                userService,
                problemService,
                answerRepository,
                problemLocalizationRepository,
                answerLocalizationRepository,
            )
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
    fun `prepareContext resolves localized text for submission language`() {
        val problem = BackendFixtures.problem(id = 1L, code = "1.1.1", images = listOf("img.png"))
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
        val submission =
            BackendFixtures.submission(id = 1L, userId = 1L, language = ContentLanguage.EN, fileObjectIds = listOf("1"))
        val answer = BackendFixtures.answer(id = 1L, problemId = 1L)
        val problemLoc = ProblemLocalization(1L, ContentLanguage.EN, "Find the speed")
        val answerLoc = AnswerLocalization(1L, ContentLanguage.EN, "v = 200 m/s")

        every { problemService.findProblemById(1L) } returns Mono.just(problem)
        every { fileObjectRepository.findAllById(listOf(1L)) } returns Flux.just(fileObject)
        every { problemLocalizationRepository.findByProblemIdAndLanguage(1L, ContentLanguage.EN) } returns
            Mono.just(problemLoc)
        every { answerRepository.findByProblemId(1L) } returns Mono.just(answer)
        every { answerLocalizationRepository.findByAnswerIdAndLanguage(1L, ContentLanguage.EN) } returns Mono.just(answerLoc)

        StepVerifier.create(mapper.prepareContext(submission, checkResultId = 10L))
            .assertNext { ctx ->
                assertThat(ctx.submissionId).isEqualTo(1L)
                assertThat(ctx.checkResultId).isEqualTo(10L)
                assertThat(ctx.problemId).isEqualTo(1L)
                assertThat(ctx.problemText).isEqualTo("Find the speed")
                assertThat(ctx.language).isEqualTo(ContentLanguage.EN)
                assertThat(ctx.problemImageRawKeys)
                    .containsExactly(ProblemFileKey("savchenko", "1.1.1", "img.png").toString())
                assertThat(ctx.submissionImageRawKeys).containsExactly(fileKey.toString())
                assertThat(ctx.answer).isEqualTo("v = 200 m/s")
            }
            .verifyComplete()
    }

    @Test
    fun `prepareContext formats subproblems into problem text when main text is empty`() {
        val problem = BackendFixtures.problem(id = 1L, code = "1.1.1")
        val submission =
            BackendFixtures.submission(id = 1L, userId = 1L, language = ContentLanguage.EN, fileObjectIds = emptyList())
        val loc =
            ProblemLocalization(
                1L,
                ContentLanguage.EN,
                "",
                subproblems = listOf(Subproblem("a", "First part"), Subproblem("b", "Second part")),
            )

        every { problemService.findProblemById(1L) } returns Mono.just(problem)
        every { fileObjectRepository.findAllById(emptyList()) } returns Flux.empty()
        every { problemLocalizationRepository.findByProblemIdAndLanguage(1L, ContentLanguage.EN) } returns Mono.just(loc)
        every { answerRepository.findByProblemId(1L) } returns Mono.empty()

        StepVerifier.create(mapper.prepareContext(submission, checkResultId = 10L))
            .assertNext { ctx -> assertThat(ctx.problemText).isEqualTo("a) First part\n\nb) Second part") }
            .verifyComplete()
    }

    @Test
    fun `prepareContext falls back when localization missing`() {
        val problem = BackendFixtures.problem(id = 1L, code = "1.1.1")
        val submission =
            BackendFixtures.submission(id = 1L, userId = 1L, language = ContentLanguage.EN, fileObjectIds = emptyList())
        val ruLoc = ProblemLocalization(1L, ContentLanguage.RU, "Задача")

        every { problemService.findProblemById(1L) } returns Mono.just(problem)
        every { fileObjectRepository.findAllById(emptyList()) } returns Flux.empty()
        every { problemLocalizationRepository.findByProblemIdAndLanguage(1L, ContentLanguage.EN) } returns Mono.empty()
        every { problemLocalizationRepository.findFirstByProblemId(1L) } returns Mono.just(ruLoc)
        every { answerRepository.findByProblemId(1L) } returns Mono.empty()

        StepVerifier.create(mapper.prepareContext(submission, checkResultId = 10L))
            .assertNext { ctx ->
                assertThat(ctx.problemText).isEqualTo("Задача")
                assertThat(ctx.answer).isNull()
            }
            .verifyComplete()
    }
}
