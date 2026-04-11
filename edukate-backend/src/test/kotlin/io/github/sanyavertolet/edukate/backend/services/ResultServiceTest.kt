@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.Result
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.storage.keys.ResultFileKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ResultServiceTest {
    private val problemRepository: ProblemRepository = mockk()
    private val fileManager: FileManager = mockk()
    private lateinit var service: ResultService

    @BeforeEach
    fun setUp() {
        service = ResultService(problemRepository, fileManager)
    }

    // region updateResult

    @Test
    fun `updateResult saves to Problem`() {
        val result = Result("1.0.0", "Answer is 42", "No notes", emptyList())
        val problem = BackendFixtures.problem(id = "1.0.0")
        val updatedProblem = problem.copy(result = result)

        every { problemRepository.findById("1.0.0") } returns Mono.just(problem)
        every { problemRepository.save(updatedProblem) } returns Mono.just(updatedProblem)

        StepVerifier.create(service.updateResult(result)).expectNext("1.0.0").verifyComplete()

        verify(exactly = 1) { problemRepository.save(updatedProblem) }
    }

    @Test
    fun `updateResultBatch saves all`() {
        val r1 = Result("1.0.0", "Ans1", "", emptyList())
        val r2 = Result("1.1.0", "Ans2", "", emptyList())
        val p1 = BackendFixtures.problem(id = "1.0.0")
        val p2 = BackendFixtures.problem(id = "1.1.0")

        every { problemRepository.findById("1.0.0") } returns Mono.just(p1)
        every { problemRepository.save(p1.copy(result = r1)) } returns Mono.just(p1.copy(result = r1))
        every { problemRepository.findById("1.1.0") } returns Mono.just(p2)
        every { problemRepository.save(p2.copy(result = r2)) } returns Mono.just(p2.copy(result = r2))

        StepVerifier.create(service.updateResultBatch(reactor.core.publisher.Flux.just(r1, r2)))
            .expectNext(2L)
            .verifyComplete()
    }

    // endregion

    // region findResultById

    @Test
    fun `findResultById returns Result with image urls`() {
        val result = Result("1.0.0", "Answer", "Notes", listOf("img.png"))
        val problem = BackendFixtures.problem(id = "1.0.0", result = result)
        val resultKey = ResultFileKey("1.0.0", "img.png")

        every { problemRepository.findById("1.0.0") } returns Mono.just(problem)
        every { fileManager.getPresignedUrl(resultKey) } returns Mono.just("https://s3/result-img.png")

        StepVerifier.create(service.findResultById("1.0.0"))
            .assertNext { r -> assertThat(r.images).containsExactly("https://s3/result-img.png") }
            .verifyComplete()
    }

    @Test
    fun `findResultById Problem not found propagates`() {
        every { problemRepository.findById("nonexistent") } returns Mono.empty()

        StepVerifier.create(service.findResultById("nonexistent")).verifyComplete()
    }

    // endregion
}
