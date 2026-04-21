@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.repositories.CheckResultRepository
import io.github.sanyavertolet.edukate.common.SubmissionStatus
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
    private val submissionService: SubmissionService = mockk()
    private val meterRegistry = SimpleMeterRegistry()
    private lateinit var service: CheckResultService

    @BeforeEach
    fun setUp() {
        service = CheckResultService(checkResultRepository, submissionService, meterRegistry)
    }

    // region saveAndUpdateSubmission

    @Test
    fun `saveAndUpdateSubmission saves check result and updates submission status`() {
        val submission = BackendFixtures.submission(id = 1L, status = SubmissionStatus.PENDING)
        val checkResult = BackendFixtures.checkResult(submissionId = 1L)
        val savedResult = checkResult.copy(id = 10L)

        every { submissionService.findById(1L) } returns Mono.just(submission)
        every { checkResultRepository.save(checkResult) } returns Mono.just(savedResult)
        every { submissionService.update(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.saveAndUpdateSubmission(checkResult))
            .assertNext { (saved, sub) ->
                assertThat(saved.id).isEqualTo(10L)
                assertThat(sub.id).isEqualTo(1L)
            }
            .verifyComplete()

        verify(exactly = 1) { submissionService.update(any()) }
    }

    @Test
    fun `saveAndUpdateSubmission emits NOT_FOUND when submission is missing`() {
        val checkResult = BackendFixtures.checkResult(submissionId = 999L)
        every { submissionService.findById(999L) } returns Mono.empty()

        StepVerifier.create(service.saveAndUpdateSubmission(checkResult))
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
