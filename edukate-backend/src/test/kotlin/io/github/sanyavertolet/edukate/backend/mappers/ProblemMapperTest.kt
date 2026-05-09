@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.configs.LanguageWebFilter
import io.github.sanyavertolet.edukate.backend.entities.Book
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.repositories.ProblemLocalizationRepository
import io.github.sanyavertolet.edukate.backend.services.AnswerService
import io.github.sanyavertolet.edukate.backend.services.BookService
import io.github.sanyavertolet.edukate.backend.services.ProblemStatusDecisionManager
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.ContentLanguage
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.util.context.Context

class ProblemMapperTest {
    private val problemStatusDecisionManager: ProblemStatusDecisionManager = mockk()
    private val fileManager: FileManager = mockk()
    private val bookService: BookService = mockk()
    private val answerService: AnswerService = mockk()
    private val problemLocalizationRepository: ProblemLocalizationRepository = mockk()
    private lateinit var mapper: ProblemMapper

    @BeforeEach
    fun setUp() {
        mapper =
            ProblemMapper(
                problemStatusDecisionManager,
                fileManager,
                bookService,
                answerService,
                problemLocalizationRepository,
            )
    }

    @Test
    fun `toDto resolves Russian localization by default`() {
        val auth = BackendFixtures.mockAuthentication()
        val problem = BackendFixtures.problem(id = 1L, code = "1.1.1", images = listOf("img.png"))
        val loc =
            BackendFixtures.problemLocalization(
                problemId = 1L,
                language = ContentLanguage.RU,
                text = "Задача",
                tags = listOf("Механика"),
            )

        every { problemStatusDecisionManager.getStatus(1L, auth) } returns Mono.just(Problem.Status.SOLVED)
        every { fileManager.getPresignedUrl(ProblemFileKey("savchenko", "1.1.1", "img.png")) } returns
            Mono.just("https://s3/img.png")
        every { answerService.hasAnswer(1L) } returns Mono.just(false)
        every { bookService.findById(any()) } returns Mono.just(BackendFixtures.book())
        every { problemLocalizationRepository.findByProblemIdAndLanguage(1L, ContentLanguage.RU) } returns Mono.just(loc)

        StepVerifier.create(
                mapper.toDto(problem, auth).contextWrite(Context.of(LanguageWebFilter.LANGUAGE_KEY, ContentLanguage.RU))
            )
            .assertNext { dto ->
                assertThat(dto.status).isEqualTo(Problem.Status.SOLVED)
                assertThat(dto.images).containsExactly("https://s3/img.png")
                assertThat(dto.text).isEqualTo("Задача")
                assertThat(dto.tags).containsExactly("Механика")
                assertThat(dto.language).isEqualTo(ContentLanguage.RU)
            }
            .verifyComplete()
    }

    @Test
    fun `toDto resolves English localization when requested`() {
        val problem = BackendFixtures.problem(id = 2L, code = "1.1.2")
        val enLoc =
            BackendFixtures.problemLocalization(
                problemId = 2L,
                language = ContentLanguage.EN,
                text = "Find the speed",
                tags = listOf("Kinematics"),
            )

        every { problemStatusDecisionManager.getStatus(2L, null) } returns Mono.just(Problem.Status.NOT_SOLVED)
        every { answerService.hasAnswer(2L) } returns Mono.just(true)
        every { bookService.findById(any()) } returns Mono.just(BackendFixtures.book(slug = "savchenko"))
        every { problemLocalizationRepository.findByProblemIdAndLanguage(2L, ContentLanguage.EN) } returns Mono.just(enLoc)

        StepVerifier.create(
                mapper.toDto(problem, null).contextWrite(Context.of(LanguageWebFilter.LANGUAGE_KEY, ContentLanguage.EN))
            )
            .assertNext { dto ->
                assertThat(dto.text).isEqualTo("Find the speed")
                assertThat(dto.tags).containsExactly("Kinematics")
                assertThat(dto.language).isEqualTo(ContentLanguage.EN)
                assertThat(dto.hasResult).isTrue()
            }
            .verifyComplete()
    }

    @Test
    fun `toDto falls back to any language when requested language is missing`() {
        val problem = BackendFixtures.problem(id = 3L, code = "5.1.1")
        val ruLoc =
            BackendFixtures.problemLocalization(
                problemId = 3L,
                language = ContentLanguage.RU,
                text = "Задача на молекулярку",
            )

        every { problemStatusDecisionManager.getStatus(3L, null) } returns Mono.just(Problem.Status.NOT_SOLVED)
        every { answerService.hasAnswer(3L) } returns Mono.just(false)
        every { bookService.findById(any()) } returns Mono.just(BackendFixtures.book())
        every { problemLocalizationRepository.findByProblemIdAndLanguage(3L, ContentLanguage.EN) } returns Mono.empty()
        every { problemLocalizationRepository.findFirstByProblemId(3L) } returns Mono.just(ruLoc)

        StepVerifier.create(
                mapper.toDto(problem, null).contextWrite(Context.of(LanguageWebFilter.LANGUAGE_KEY, ContentLanguage.EN))
            )
            .assertNext { dto ->
                assertThat(dto.text).isEqualTo("Задача на молекулярку")
                assertThat(dto.language).isEqualTo(ContentLanguage.RU)
            }
            .verifyComplete()
    }

    @Test
    fun `toMetadata resolves localized tags`() {
        val auth = BackendFixtures.mockAuthentication()
        val problem = BackendFixtures.problem(id = 1L, code = "1.1.1")
        val loc =
            BackendFixtures.problemLocalization(
                problemId = 1L,
                language = ContentLanguage.EN,
                tags = listOf("Constant velocity motion"),
            )

        every { problemStatusDecisionManager.getStatus(1L, auth) } returns Mono.just(Problem.Status.NOT_SOLVED)
        every { bookService.findById(any()) } returns Mono.just(BackendFixtures.book())
        every { problemLocalizationRepository.findByProblemIdAndLanguage(1L, ContentLanguage.EN) } returns Mono.just(loc)

        StepVerifier.create(
                mapper.toMetadata(problem, auth).contextWrite(Context.of(LanguageWebFilter.LANGUAGE_KEY, ContentLanguage.EN))
            )
            .assertNext { meta ->
                assertThat(meta.tags).containsExactly("Constant velocity motion")
                assertThat(meta.language).isEqualTo(ContentLanguage.EN)
            }
            .verifyComplete()
    }

    @Test
    fun `toMetadata propagates createdAt from entity`() {
        val auth = BackendFixtures.mockAuthentication()
        val timestamp = Instant.parse("2026-03-15T12:00:00Z")
        val problem = BackendFixtures.problem(id = 1L, code = "2.1.1", createdAt = timestamp)
        every { problemStatusDecisionManager.getStatus(1L, auth) } returns Mono.just(Problem.Status.NOT_SOLVED)
        every { bookService.findById(any()) } returns Mono.just(BackendFixtures.book())
        every { problemLocalizationRepository.findByProblemIdAndLanguage(1L, ContentLanguage.RU) } returns
            Mono.just(BackendFixtures.problemLocalization(problemId = 1L))

        StepVerifier.create(
                mapper.toMetadata(problem, auth).contextWrite(Context.of(LanguageWebFilter.LANGUAGE_KEY, ContentLanguage.RU))
            )
            .assertNext { meta ->
                assertThat(meta.createdAt).isEqualTo(timestamp)
                assertThat(meta.code).isEqualTo("2.1.1")
            }
            .verifyComplete()
    }

    @Test
    fun `toMetadata defaults bookSlug to unknown when book is missing`() {
        val problem = BackendFixtures.problem(id = 1L, bookId = 999L)
        every { problemStatusDecisionManager.getStatus(1L, null) } returns Mono.just(Problem.Status.NOT_SOLVED)
        every { bookService.findById(999L) } returns Mono.empty<Book>()
        every { problemLocalizationRepository.findByProblemIdAndLanguage(1L, ContentLanguage.RU) } returns
            Mono.just(BackendFixtures.problemLocalization(problemId = 1L))

        StepVerifier.create(
                mapper.toMetadata(problem, null).contextWrite(Context.of(LanguageWebFilter.LANGUAGE_KEY, ContentLanguage.RU))
            )
            .assertNext { meta -> assertThat(meta.bookSlug).isEqualTo("unknown") }
            .verifyComplete()
    }
}
