@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.Book
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.services.AnswerService
import io.github.sanyavertolet.edukate.backend.services.BookService
import io.github.sanyavertolet.edukate.backend.services.ProblemStatusDecisionManager
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProblemMapperTest {
    private val problemStatusDecisionManager: ProblemStatusDecisionManager = mockk()
    private val fileManager: FileManager = mockk()
    private val bookService: BookService = mockk()
    private val answerService: AnswerService = mockk()
    private lateinit var mapper: ProblemMapper

    @BeforeEach
    fun setUp() {
        mapper = ProblemMapper(problemStatusDecisionManager, fileManager, bookService, answerService)
    }

    @Test
    fun `toDto zips status and images`() {
        val auth = BackendFixtures.mockAuthentication()
        val problem = BackendFixtures.problem(id = 1L, code = "1.1.1", images = listOf("img.png"))
        every { problemStatusDecisionManager.getStatus(1L, auth) } returns Mono.just(Problem.Status.SOLVED)
        every { fileManager.getPresignedUrl(ProblemFileKey("savchenko", "1.1.1", "img.png")) } returns
            Mono.just("https://s3/img.png")
        every { answerService.hasAnswer(1L) } returns Mono.just(false)
        every { bookService.findById(any()) } returns Mono.just(BackendFixtures.book())

        StepVerifier.create(mapper.toDto(problem, auth))
            .assertNext { dto ->
                assertThat(dto.status).isEqualTo(Problem.Status.SOLVED)
                assertThat(dto.images).containsExactly("https://s3/img.png")
            }
            .verifyComplete()
    }

    @Test
    fun `toMetadata applies status`() {
        val auth = BackendFixtures.mockAuthentication()
        val problem = BackendFixtures.problem(id = 1L, code = "1.1.1")
        every { problemStatusDecisionManager.getStatus(1L, auth) } returns Mono.just(Problem.Status.SOLVING)
        every { bookService.findById(any()) } returns Mono.just(BackendFixtures.book())

        StepVerifier.create(mapper.toMetadata(problem, auth))
            .assertNext { meta -> assertThat(meta.status).isEqualTo(Problem.Status.SOLVING) }
            .verifyComplete()
    }

    @Test
    fun `toMetadata propagates createdAt from entity`() {
        val auth = BackendFixtures.mockAuthentication()
        val timestamp = Instant.parse("2026-03-15T12:00:00Z")
        val problem = BackendFixtures.problem(id = 1L, code = "2.1.1", createdAt = timestamp)
        every { problemStatusDecisionManager.getStatus(1L, auth) } returns Mono.just(Problem.Status.NOT_SOLVED)
        every { bookService.findById(any()) } returns Mono.just(BackendFixtures.book())

        StepVerifier.create(mapper.toMetadata(problem, auth))
            .assertNext { meta ->
                assertThat(meta.createdAt).isEqualTo(timestamp)
                assertThat(meta.code).isEqualTo("2.1.1")
            }
            .verifyComplete()
    }

    @Test
    fun `toMetadata resolves bookSlug from book service`() {
        val auth = BackendFixtures.mockAuthentication()
        val problem = BackendFixtures.problem(id = 1L, bookId = 5L)
        every { problemStatusDecisionManager.getStatus(1L, auth) } returns Mono.just(Problem.Status.NOT_SOLVED)
        every { bookService.findById(5L) } returns Mono.just(BackendFixtures.book(id = 5L, slug = "irodov"))

        StepVerifier.create(mapper.toMetadata(problem, auth))
            .assertNext { meta -> assertThat(meta.bookSlug).isEqualTo("irodov") }
            .verifyComplete()
    }

    @Test
    fun `toMetadata defaults bookSlug to unknown when book is missing`() {
        val problem = BackendFixtures.problem(id = 1L, bookId = 999L)
        every { problemStatusDecisionManager.getStatus(1L, null) } returns Mono.just(Problem.Status.NOT_SOLVED)
        every { bookService.findById(999L) } returns Mono.empty<Book>()

        StepVerifier.create(mapper.toMetadata(problem, null))
            .assertNext { meta -> assertThat(meta.bookSlug).isEqualTo("unknown") }
            .verifyComplete()
    }
}
