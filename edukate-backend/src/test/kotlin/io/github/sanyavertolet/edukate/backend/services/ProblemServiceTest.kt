@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.filters.ProblemFilter
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProblemServiceTest {
    private val problemRepository: ProblemRepository = mockk()
    private val bookService: BookService = mockk()
    private lateinit var service: ProblemService

    @BeforeEach
    fun setUp() {
        service = ProblemService(problemRepository, bookService)
    }

    // region getFilteredProblems

    @Test
    fun `getFilteredProblems returns problems`() {
        val page = PageRequest.of(0, 10)
        val problems =
            listOf(BackendFixtures.problem(id = 1L, code = "1.1.1"), BackendFixtures.problem(id = 2L, code = "1.1.2"))
        every {
            problemRepository.findWithFilter(null, null, null, null, null, false, null, null, page.pageSize, page.offset)
        } returns Flux.fromIterable(problems)

        StepVerifier.create(service.getFilteredProblems(ProblemFilter(), null, page)).expectNextCount(2).verifyComplete()

        verify(exactly = 1) {
            problemRepository.findWithFilter(null, null, null, null, null, false, null, null, page.pageSize, page.offset)
        }
    }

    // endregion

    // region findProblemById / findProblemsByIds

    @Test
    fun `findProblemById returns Problem`() {
        val problem = BackendFixtures.problem(id = 1L)
        every { problemRepository.findById(1L) } returns Mono.just(problem)

        StepVerifier.create(service.findProblemById(1L)).expectNext(problem).verifyComplete()
    }

    @Test
    fun `findProblemsByIds returns all`() {
        val p1 = BackendFixtures.problem(id = 1L)
        val p2 = BackendFixtures.problem(id = 2L)
        every { problemRepository.findByIdIn(listOf(1L, 2L)) } returns Flux.just(p1, p2)

        StepVerifier.create(service.findProblemsByIds(listOf(1L, 2L))).expectNext(p1, p2).verifyComplete()
    }

    // endregion

    // region updateProblem / updateProblemBatch

    @Test
    fun `updateProblem saves to repo`() {
        val problem = BackendFixtures.problem(id = 1L)
        every { problemRepository.save(problem) } returns Mono.just(problem)

        StepVerifier.create(service.updateProblem(problem)).expectNext(problem).verifyComplete()

        verify(exactly = 1) { problemRepository.save(problem) }
    }

    @Test
    fun `updateProblemBatch saves all`() {
        val p1 = BackendFixtures.problem(id = 1L)
        val p2 = BackendFixtures.problem(id = 2L)
        every { problemRepository.saveAll(any<Flux<Problem>>()) } returns Flux.just(p1, p2)

        StepVerifier.create(service.updateProblemBatch(Flux.just(p1, p2))).expectNext(2L).verifyComplete()

        verify(exactly = 1) { problemRepository.saveAll(any<Flux<Problem>>()) }
    }

    // endregion

    // region countFilteredProblems / deleteProblemById

    @Test
    fun `countFilteredProblems with no filters returns total count`() {
        every { problemRepository.countWithFilter(null, null, null, null, null, false, null, null) } returns Mono.just(42L)

        StepVerifier.create(service.countFilteredProblems(ProblemFilter(), null)).expectNext(42L).verifyComplete()
    }

    @Test
    fun `deleteProblemById delegates`() {
        every { problemRepository.deleteById(1L) } returns Mono.empty()

        StepVerifier.create(service.deleteProblemById(1L)).verifyComplete()

        verify(exactly = 1) { problemRepository.deleteById(1L) }
    }

    // endregion

    // region getRandomUnsolvedProblemKey

    @Test
    fun `getRandomUnsolvedProblemKey prefers unsolved`() {
        val auth = BackendFixtures.mockAuthentication(userId = 1L)
        every { problemRepository.findRandomUnsolvedProblem(1L) } returns
            Mono.just(BackendFixtures.problem(id = 2L, code = "1.1.2"))
        every { problemRepository.findRandomProblem() } returns Mono.just(BackendFixtures.problem(id = 99L, code = "1.1.99"))

        StepVerifier.create(service.getRandomUnsolvedProblemKey(auth)).expectNext("savchenko/1.1.2").verifyComplete()
    }

    @Test
    fun `getRandomUnsolvedProblemKey falls back to random`() {
        val auth = BackendFixtures.mockAuthentication(userId = 1L)
        every { problemRepository.findRandomUnsolvedProblem(1L) } returns Mono.empty()
        every { problemRepository.findRandomProblem() } returns Mono.just(BackendFixtures.problem(id = 3L, code = "1.1.3"))

        StepVerifier.create(service.getRandomUnsolvedProblemKey(auth)).expectNext("savchenko/1.1.3").verifyComplete()
    }

    @Test
    fun `getRandomUnsolvedProblemKey when unauthenticated`() {
        every { problemRepository.findRandomProblem() } returns Mono.just(BackendFixtures.problem(id = 1L, code = "1.1.1"))

        StepVerifier.create(service.getRandomUnsolvedProblemKey(null)).expectNext("savchenko/1.1.1").verifyComplete()
    }

    // endregion
}
