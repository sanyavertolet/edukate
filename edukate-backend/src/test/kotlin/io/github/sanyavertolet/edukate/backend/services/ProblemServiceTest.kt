@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.filters.ProblemFilter
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
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
    private val fileManager: FileManager = mockk()
    private val problemStatusDecisionManager: ProblemStatusDecisionManager = mockk()
    private lateinit var service: ProblemService

    @BeforeEach
    fun setUp() {
        service = ProblemService(problemRepository, mongoTemplate, fileManager, problemStatusDecisionManager)
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

    // region problemImageDownloadUrls / prepareDto / prepareMetadata

    @Test
    fun `problemImageDownloadUrls generates presigned urls`() {
        every { fileManager.getPresignedUrl(ProblemFileKey("1.0.0", "img.png")) } returns Mono.just("https://s3/img.png")

        StepVerifier.create(service.problemImageDownloadUrls("1.0.0", listOf("img.png")))
            .expectNext("https://s3/img.png")
            .verifyComplete()
    }

    @Test
    fun `prepareDto zips status and images`() {
        val auth = BackendFixtures.mockAuthentication()
        val problem = BackendFixtures.problem(id = "1.0.0", images = listOf("img.png"))
        every { problemStatusDecisionManager.getStatus("1.0.0", auth) } returns Mono.just(Problem.Status.SOLVED)
        every { fileManager.getPresignedUrl(ProblemFileKey("1.0.0", "img.png")) } returns Mono.just("https://s3/img.png")

        StepVerifier.create(service.prepareDto(problem, auth))
            .assertNext { dto ->
                assertThat(dto.status).isEqualTo(Problem.Status.SOLVED)
                assertThat(dto.images).containsExactly("https://s3/img.png")
            }
            .verifyComplete()
    }

    @Test
    fun `prepareMetadata applies status`() {
        val auth = BackendFixtures.mockAuthentication()
        val problem = BackendFixtures.problem(id = "1.0.0")
        every { problemStatusDecisionManager.getStatus("1.0.0", auth) } returns Mono.just(Problem.Status.SOLVING)

        StepVerifier.create(service.prepareMetadata(problem, auth))
            .assertNext { meta -> assertThat(meta.status).isEqualTo(Problem.Status.SOLVING) }
            .verifyComplete()
    }

    // endregion
}
