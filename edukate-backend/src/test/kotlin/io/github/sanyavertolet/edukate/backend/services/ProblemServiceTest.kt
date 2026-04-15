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
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProblemServiceTest {
    private val problemRepository: ProblemRepository = mockk()
    private val mongoTemplate: ReactiveMongoTemplate = mockk()
    private lateinit var service: ProblemService

    @BeforeEach
    fun setUp() {
        service = ProblemService(problemRepository, mongoTemplate)
    }

    // region getFilteredProblems

    @Test
    fun `getFilteredProblems applies SemVerSort`() {
        val page = PageRequest.of(0, 10)
        val problems = listOf(BackendFixtures.problem("1.0.0"), BackendFixtures.problem("1.1.0"))
        every { problemRepository.findAll(any(Pageable::class)) } returns Flux.fromIterable(problems)

        StepVerifier.create(service.getFilteredProblems(ProblemFilter(), null, page)).expectNextCount(2).verifyComplete()

        verify(exactly = 1) { problemRepository.findAll(any(Pageable::class)) }
    }

    // endregion

    // region findProblemById / findProblemsByIds

    @Test
    fun `findProblemById returns Problem`() {
        val problem = BackendFixtures.problem("1.0.0")
        every { problemRepository.findById("1.0.0") } returns Mono.just(problem)

        StepVerifier.create(service.findProblemById("1.0.0")).expectNext(problem).verifyComplete()
    }

    @Test
    fun `findProblemsByIds returns all`() {
        val p1 = BackendFixtures.problem("1.0.0")
        val p2 = BackendFixtures.problem("1.1.0")
        every { problemRepository.findProblemsByIdIn(listOf("1.0.0", "1.1.0")) } returns Flux.just(p1, p2)

        StepVerifier.create(service.findProblemsByIds(listOf("1.0.0", "1.1.0"))).expectNext(p1, p2).verifyComplete()
    }

    // endregion

    // region updateProblem / updateProblemBatch

    @Test
    fun `updateProblem saves to repo`() {
        val problem = BackendFixtures.problem("1.0.0")
        every { problemRepository.save(problem) } returns Mono.just(problem)

        StepVerifier.create(service.updateProblem(problem)).expectNext(problem).verifyComplete()

        verify(exactly = 1) { problemRepository.save(problem) }
    }

    @Test
    fun `updateProblemBatch saves all`() {
        val p1 = BackendFixtures.problem("1.0.0")
        val p2 = BackendFixtures.problem("1.1.0")
        every { problemRepository.saveAll(any<Flux<Problem>>()) } returns Flux.just(p1, p2)

        StepVerifier.create(service.updateProblemBatch(Flux.just(p1, p2))).expectNext(2L).verifyComplete()

        verify(exactly = 1) { problemRepository.saveAll(any<Flux<Problem>>()) }
    }

    // endregion

    // region countFilteredProblems / deleteProblemById

    @Test
    fun `countFilteredProblems with no filters returns total count`() {
        every { problemRepository.count() } returns Mono.just(42L)

        StepVerifier.create(service.countFilteredProblems(ProblemFilter(), null)).expectNext(42L).verifyComplete()
    }

    @Test
    fun `deleteProblemById delegates`() {
        every { problemRepository.deleteById("1.0.0") } returns Mono.empty()

        StepVerifier.create(service.deleteProblemById("1.0.0")).verifyComplete()

        verify(exactly = 1) { problemRepository.deleteById("1.0.0") }
    }

    // endregion

    // region getRandomUnsolvedProblemId

    @Test
    fun `getRandomUnsolvedProblemId prefers unsolved`() {
        val auth = BackendFixtures.mockAuthentication(userId = "user-1")
        every { problemRepository.findRandomUnsolvedProblemId("user-1") } returns Mono.just("2.0.0")
        // switchIfEmpty evaluates its argument eagerly (chain construction, not subscription),
        // so findRandomProblemId() must be stubbed even though its returned Mono is never
        // subscribed.
        every { problemRepository.findRandomProblemId() } returns Mono.just("fallback")

        StepVerifier.create(service.getRandomUnsolvedProblemId(auth)).expectNext("2.0.0").verifyComplete()
    }

    @Test
    fun `getRandomUnsolvedProblemId falls back to random`() {
        val auth = BackendFixtures.mockAuthentication(userId = "user-1")
        every { problemRepository.findRandomUnsolvedProblemId("user-1") } returns Mono.empty()
        every { problemRepository.findRandomProblemId() } returns Mono.just("3.0.0")

        StepVerifier.create(service.getRandomUnsolvedProblemId(auth)).expectNext("3.0.0").verifyComplete()
    }

    @Test
    fun `getRandomUnsolvedProblemId when unauthenticated`() {
        every { problemRepository.findRandomProblemId() } returns Mono.just("1.0.0")

        StepVerifier.create(service.getRandomUnsolvedProblemId(null)).expectNext("1.0.0").verifyComplete()
    }

    // endregion

}
