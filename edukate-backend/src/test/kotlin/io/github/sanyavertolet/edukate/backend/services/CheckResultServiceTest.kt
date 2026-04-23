@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.repositories.CheckResultRepository
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class CheckResultServiceTest {

    private val checkResultRepository: CheckResultRepository = mockk()
    private val meterRegistry = SimpleMeterRegistry()
    private lateinit var service: CheckResultService

    @BeforeEach
    fun setUp() {
        service = CheckResultService(checkResultRepository, meterRegistry)
    }

    // region saveCheckResult

    @Test
    fun `saveCheckResult persists the result and increments counter`() {
        val checkResult = BackendFixtures.checkResult(id = null, submissionId = 1L, status = CheckStatus.SUCCESS)
        val saved = checkResult.copy(id = 10L)

        every { checkResultRepository.save(checkResult) } returns Mono.just(saved)

        StepVerifier.create(service.saveCheckResult(checkResult))
            .assertNext { assertThat(it.id).isEqualTo(10L) }
            .verifyComplete()

        assertThat(meterRegistry.counter("check.outcomes", "status", "SUCCESS").count()).isEqualTo(1.0)
    }

    // endregion

    // region updateFromMessage

    @Test
    fun `updateFromMessage looks up stub by checkResultId, updates it, and increments counter`() {
        val message = BackendFixtures.checkResultMessage(submissionId = 5L, checkResultId = 3L, status = CheckStatus.SUCCESS)
        val stub = BackendFixtures.checkResult(id = 3L, submissionId = 5L, status = CheckStatus.PENDING)
        val updated =
            stub.copy(status = CheckStatus.SUCCESS, trustLevel = message.trustLevel, explanation = message.explanation)

        every { checkResultRepository.findById(3L) } returns Mono.just(stub)
        every { checkResultRepository.save(any()) } returns Mono.just(updated)

        StepVerifier.create(service.updateFromMessage(message))
            .assertNext { saved -> assertThat(saved.status).isEqualTo(CheckStatus.SUCCESS) }
            .verifyComplete()

        verify(exactly = 1) { checkResultRepository.save(any()) }
        assertThat(meterRegistry.counter("check.outcomes", "status", "SUCCESS").count()).isEqualTo(1.0)
    }

    @Test
    fun `updateFromMessage emits NOT_FOUND when checkResultId does not exist`() {
        val message = BackendFixtures.checkResultMessage(checkResultId = 999L)

        every { checkResultRepository.findById(999L) } returns Mono.empty()

        StepVerifier.create(service.updateFromMessage(message))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.NOT_FOUND }
            .verify()

        verify(exactly = 0) { checkResultRepository.save(any()) }
    }

    // endregion

    // region findById

    @Test
    fun `findById delegates to repository`() {
        val result = BackendFixtures.checkResult(id = 1L)
        every { checkResultRepository.findById(1L) } returns Mono.just(result)

        StepVerifier.create(service.findById(1L)).expectNext(result).verifyComplete()
    }

    @Test
    fun `findById returns empty Mono when not found`() {
        every { checkResultRepository.findById(999L) } returns Mono.empty()

        StepVerifier.create(service.findById(999L)).verifyComplete()
    }

    // endregion

    // region findAllBySubmissionId

    @Test
    fun `findAllBySubmissionId delegates to repository sorted by createdAt DESC`() {
        val r1 = BackendFixtures.checkResult(id = 1L, submissionId = 1L)
        val r2 = BackendFixtures.checkResult(id = 2L, submissionId = 1L)
        val expectedSort = Sort.by(Sort.Direction.DESC, "createdAt")
        every { checkResultRepository.findBySubmissionId(1L, expectedSort) } returns Flux.just(r1, r2)

        StepVerifier.create(service.findAllBySubmissionId(1L)).expectNext(r1, r2).verifyComplete()
    }

    @Test
    fun `findAllBySubmissionId returns empty flux when no results exist`() {
        val expectedSort = Sort.by(Sort.Direction.DESC, "createdAt")
        every { checkResultRepository.findBySubmissionId(99L, expectedSort) } returns Flux.empty()

        StepVerifier.create(service.findAllBySubmissionId(99L)).verifyComplete()
    }

    // endregion
}
