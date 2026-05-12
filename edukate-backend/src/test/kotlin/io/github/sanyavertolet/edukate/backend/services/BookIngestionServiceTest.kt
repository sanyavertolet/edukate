@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.ingestion.AnswerIngestionEntry
import io.github.sanyavertolet.edukate.backend.dtos.ingestion.AnswerLocalizationData
import io.github.sanyavertolet.edukate.backend.dtos.ingestion.BookData
import io.github.sanyavertolet.edukate.backend.dtos.ingestion.BookIngestionRequest
import io.github.sanyavertolet.edukate.backend.dtos.ingestion.ProblemIngestionEntry
import io.github.sanyavertolet.edukate.backend.dtos.ingestion.ProblemLocalizationData
import io.github.sanyavertolet.edukate.backend.repositories.AnswerLocalizationRepository
import io.github.sanyavertolet.edukate.backend.repositories.AnswerRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemLocalizationRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.common.ContentLanguage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class BookIngestionServiceTest {
    private val bookService: BookService = mockk()
    private val problemRepository: ProblemRepository = mockk()
    private val answerRepository: AnswerRepository = mockk()
    private val problemLocalizationRepository: ProblemLocalizationRepository = mockk()
    private val answerLocalizationRepository: AnswerLocalizationRepository = mockk()
    private lateinit var service: BookIngestionService

    @BeforeEach
    fun setUp() {
        service =
            BookIngestionService(
                bookService,
                problemRepository,
                answerRepository,
                problemLocalizationRepository,
                answerLocalizationRepository,
            )
    }

    private fun bookData(slug: String = "savchenko") =
        BookData(
            slug = slug,
            subject = "physics",
            title = "Savchenko",
            citation = "Savchenko O.Ya., 1988",
            description = null,
        )

    private fun problemEntry(
        code: String = "1.1.1",
        localizations: Map<ContentLanguage, ProblemLocalizationData> =
            mapOf(ContentLanguage.RU to ProblemLocalizationData(text = "Тело массой m...")),
        answer: AnswerIngestionEntry? = null,
    ) = ProblemIngestionEntry(code = code, localizations = localizations, answer = answer)

    private fun answerEntry(
        localizations: Map<ContentLanguage, AnswerLocalizationData> =
            mapOf(ContentLanguage.RU to AnswerLocalizationData(text = "v = 10 м/с"))
    ) = AnswerIngestionEntry(localizations = localizations)

    @Test
    fun `ingest creates new book and problem when neither exists`() {
        val book = BackendFixtures.book(id = 1L, slug = "savchenko")
        val problem = BackendFixtures.problem(id = 1L, code = "1.1.1", key = "savchenko/1.1.1")

        every { bookService.findBySlug("savchenko") } returns Mono.empty()
        every { bookService.save(any()) } returns Mono.just(book)
        every { problemRepository.findByKey("savchenko/1.1.1") } returns Mono.empty()
        every { problemRepository.save(any()) } returns Mono.just(problem)
        every { problemLocalizationRepository.upsert(1L, ContentLanguage.RU, any(), any(), any()) } returns Mono.just(1)

        val request = BookIngestionRequest(bookData(), listOf(problemEntry()))

        StepVerifier.create(service.ingest(request))
            .assertNext { response ->
                assertThat(response.bookSlug).isEqualTo("savchenko")
                assertThat(response.problemCount).isEqualTo(1)
                assertThat(response.problemLocalizationCount).isEqualTo(1)
                assertThat(response.answerCount).isEqualTo(0)
                assertThat(response.answerLocalizationCount).isEqualTo(0)
            }
            .verifyComplete()

        verify(exactly = 1) { bookService.save(any()) }
        verify(exactly = 1) { problemRepository.save(any()) }
        verify(exactly = 1) { problemLocalizationRepository.upsert(1L, ContentLanguage.RU, any(), any(), any()) }
    }

    @Test
    fun `ingest updates existing book and problem`() {
        val existingBook = BackendFixtures.book(id = 5L, slug = "savchenko")
        val existingProblem = BackendFixtures.problem(id = 2L, key = "savchenko/1.1.1")

        every { bookService.findBySlug("savchenko") } returns Mono.just(existingBook)
        every { bookService.save(any()) } returns Mono.just(existingBook)
        every { problemRepository.findByKey("savchenko/1.1.1") } returns Mono.just(existingProblem)
        every { problemRepository.save(any()) } returns Mono.just(existingProblem)
        every { problemLocalizationRepository.upsert(2L, ContentLanguage.RU, any(), any(), any()) } returns Mono.just(1)

        StepVerifier.create(service.ingest(BookIngestionRequest(bookData(), listOf(problemEntry()))))
            .assertNext { response ->
                assertThat(response.problemCount).isEqualTo(1)
                assertThat(response.problemLocalizationCount).isEqualTo(1)
            }
            .verifyComplete()

        verify(exactly = 1) { bookService.save(match { it.id == 5L }) }
        verify(exactly = 1) { problemRepository.save(match { it.id == 2L }) }
    }

    @Test
    fun `ingest with answer creates answer and answer localization`() {
        val book = BackendFixtures.book(id = 1L, slug = "savchenko")
        val problem = BackendFixtures.problem(id = 1L, key = "savchenko/1.1.1")
        val answer = BackendFixtures.answer(id = 10L, problemId = 1L)

        every { bookService.findBySlug("savchenko") } returns Mono.empty()
        every { bookService.save(any()) } returns Mono.just(book)
        every { problemRepository.findByKey("savchenko/1.1.1") } returns Mono.empty()
        every { problemRepository.save(any()) } returns Mono.just(problem)
        every { problemLocalizationRepository.upsert(1L, ContentLanguage.RU, any(), any(), any()) } returns Mono.just(1)
        every { answerRepository.findByProblemId(1L) } returns Mono.empty()
        every { answerRepository.save(any()) } returns Mono.just(answer)
        every { answerLocalizationRepository.upsert(10L, ContentLanguage.RU, "v = 10 м/с", null) } returns Mono.just(1)

        StepVerifier.create(service.ingest(BookIngestionRequest(bookData(), listOf(problemEntry(answer = answerEntry())))))
            .assertNext { response ->
                assertThat(response.answerCount).isEqualTo(1)
                assertThat(response.answerLocalizationCount).isEqualTo(1)
            }
            .verifyComplete()

        verify(exactly = 1) { answerRepository.save(any()) }
        verify(exactly = 1) { answerLocalizationRepository.upsert(10L, ContentLanguage.RU, "v = 10 м/с", null) }
    }

    @Test
    fun `ingest without answer skips answer repositories`() {
        val book = BackendFixtures.book(id = 1L, slug = "savchenko")
        val problem = BackendFixtures.problem(id = 1L, key = "savchenko/1.1.1")

        every { bookService.findBySlug("savchenko") } returns Mono.empty()
        every { bookService.save(any()) } returns Mono.just(book)
        every { problemRepository.findByKey("savchenko/1.1.1") } returns Mono.empty()
        every { problemRepository.save(any()) } returns Mono.just(problem)
        every { problemLocalizationRepository.upsert(1L, ContentLanguage.RU, any(), any(), any()) } returns Mono.just(1)

        StepVerifier.create(service.ingest(BookIngestionRequest(bookData(), listOf(problemEntry(answer = null)))))
            .assertNext { response -> assertThat(response.answerCount).isEqualTo(0) }
            .verifyComplete()

        verify(exactly = 0) { answerRepository.save(any()) }
        verify(exactly = 0) { answerLocalizationRepository.upsert(any(), any(), any(), any()) }
    }

    @Test
    fun `ingest with two localizations upserts both`() {
        val book = BackendFixtures.book(id = 1L, slug = "savchenko")
        val problem = BackendFixtures.problem(id = 1L, key = "savchenko/1.1.1")

        every { bookService.findBySlug("savchenko") } returns Mono.empty()
        every { bookService.save(any()) } returns Mono.just(book)
        every { problemRepository.findByKey("savchenko/1.1.1") } returns Mono.empty()
        every { problemRepository.save(any()) } returns Mono.just(problem)
        every { problemLocalizationRepository.upsert(1L, any(), any(), any(), any()) } returns Mono.just(1)

        val localizations =
            mapOf(
                ContentLanguage.RU to ProblemLocalizationData("Тело массой m..."),
                ContentLanguage.EN to ProblemLocalizationData("A body of mass m..."),
            )

        StepVerifier.create(
                service.ingest(BookIngestionRequest(bookData(), listOf(problemEntry(localizations = localizations))))
            )
            .assertNext { response -> assertThat(response.problemLocalizationCount).isEqualTo(2) }
            .verifyComplete()

        verify(exactly = 1) { problemLocalizationRepository.upsert(1L, ContentLanguage.RU, any(), any(), any()) }
        verify(exactly = 1) { problemLocalizationRepository.upsert(1L, ContentLanguage.EN, any(), any(), any()) }
    }

    @Test
    fun `ingest existing localizations completes without error`() {
        val book = BackendFixtures.book(id = 1L, slug = "savchenko")
        val problem = BackendFixtures.problem(id = 1L, key = "savchenko/1.1.1")

        every { bookService.findBySlug("savchenko") } returns Mono.just(book)
        every { bookService.save(any()) } returns Mono.just(book)
        every { problemRepository.findByKey("savchenko/1.1.1") } returns Mono.just(problem)
        every { problemRepository.save(any()) } returns Mono.just(problem)
        every { problemLocalizationRepository.upsert(1L, ContentLanguage.RU, any(), any(), any()) } returns Mono.just(1)

        StepVerifier.create(service.ingest(BookIngestionRequest(bookData(), listOf(problemEntry()))))
            .assertNext {}
            .verifyComplete()
    }

    @Test
    fun `ingest multiple problems returns correct aggregate counts`() {
        val book = BackendFixtures.book(id = 1L, slug = "savchenko")
        val problem1 = BackendFixtures.problem(id = 1L, code = "1.1.1", key = "savchenko/1.1.1")
        val problem2 = BackendFixtures.problem(id = 2L, code = "1.1.2", key = "savchenko/1.1.2")
        val answer2 = BackendFixtures.answer(id = 20L, problemId = 2L)

        every { bookService.findBySlug("savchenko") } returns Mono.empty()
        every { bookService.save(any()) } returns Mono.just(book)
        every { problemRepository.findByKey("savchenko/1.1.1") } returns Mono.empty()
        every { problemRepository.findByKey("savchenko/1.1.2") } returns Mono.empty()
        every { problemRepository.save(match { it.code == "1.1.1" }) } returns Mono.just(problem1)
        every { problemRepository.save(match { it.code == "1.1.2" }) } returns Mono.just(problem2)
        every { problemLocalizationRepository.upsert(any(), any(), any(), any(), any()) } returns Mono.just(1)
        every { answerRepository.findByProblemId(2L) } returns Mono.empty()
        every { answerRepository.save(any()) } returns Mono.just(answer2)
        every { answerLocalizationRepository.upsert(20L, ContentLanguage.RU, any(), any()) } returns Mono.just(1)

        val request =
            BookIngestionRequest(
                bookData(),
                listOf(problemEntry(code = "1.1.1"), problemEntry(code = "1.1.2", answer = answerEntry())),
            )

        StepVerifier.create(service.ingest(request))
            .assertNext { response ->
                assertThat(response.problemCount).isEqualTo(2)
                assertThat(response.problemLocalizationCount).isEqualTo(2)
                assertThat(response.answerCount).isEqualTo(1)
                assertThat(response.answerLocalizationCount).isEqualTo(1)
            }
            .verifyComplete()
    }
}
