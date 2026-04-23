@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.Answer
import io.github.sanyavertolet.edukate.backend.repositories.AnswerRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.storage.keys.AnswerFileKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class AnswerServiceTest {
    private val answerRepository: AnswerRepository = mockk()
    private val problemRepository: ProblemRepository = mockk()
    private val fileManager: FileManager = mockk()
    private lateinit var service: AnswerService

    @BeforeEach
    fun setUp() {
        service = AnswerService(answerRepository, problemRepository, fileManager)
    }

    // region saveAnswer

    @Test
    fun `saveAnswer saves to repository`() {
        val answer = BackendFixtures.answer(id = null, problemId = 1L)
        val savedAnswer = answer.copy(id = 1L)

        every { answerRepository.save(answer) } returns Mono.just(savedAnswer)

        StepVerifier.create(service.saveAnswer(answer)).expectNext(savedAnswer).verifyComplete()

        verify(exactly = 1) { answerRepository.save(answer) }
    }

    @Test
    fun `saveAnswerBatch saves all`() {
        val a1 = BackendFixtures.answer(id = null, problemId = 1L)
        val a2 = BackendFixtures.answer(id = null, problemId = 2L)

        every { answerRepository.saveAll(any<Publisher<Answer>>()) } returns Flux.just(a1.copy(id = 1L), a2.copy(id = 2L))

        StepVerifier.create(service.saveAnswerBatch(Flux.just(a1, a2))).expectNext(2L).verifyComplete()
    }

    // endregion

    // region findByProblemKey

    @Test
    fun `findByProblemKey returns AnswerDto with image urls`() {
        val problem = BackendFixtures.problem(id = 1L, code = "P1")
        val answer =
            BackendFixtures.answer(id = 1L, problemId = 1L, text = "Answer", notes = "Notes", images = listOf("img.png"))

        every { problemRepository.findByKey("savchenko/P1") } returns Mono.just(problem)
        every { answerRepository.findByProblemId(1L) } returns Mono.just(answer)
        every { fileManager.getPresignedUrl(AnswerFileKey("savchenko", "P1", "img.png")) } returns
            Mono.just("https://s3/result-img.png")

        StepVerifier.create(service.findByProblemKey("savchenko/P1"))
            .assertNext { r -> assertThat(r.images).containsExactly("https://s3/result-img.png") }
            .verifyComplete()
    }

    @Test
    fun `findByProblemKey returns empty when problem not found`() {
        every { problemRepository.findByKey("UNKNOWN") } returns Mono.empty()

        StepVerifier.create(service.findByProblemKey("UNKNOWN")).verifyComplete()
    }

    // endregion
}
