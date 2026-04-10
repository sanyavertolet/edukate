@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.repositories.CheckResultRepository
import io.github.sanyavertolet.edukate.common.SubmissionStatus
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
    private lateinit var service: CheckResultService

    @BeforeEach
    fun setUp() {
        service = CheckResultService(checkResultRepository, submissionService)
    }

    // region saveAndUpdateSubmission

    @Test
    fun `saveAndUpdateSubmission saves check result and updates submission status`() {
        val submission = BackendFixtures.submission(id = "sub-1", status = SubmissionStatus.PENDING)
        val checkResult = BackendFixtures.checkResult(submissionId = "sub-1")
        val savedResult = checkResult.copy(id = "cr-saved")

        every { submissionService.findById("sub-1") } returns Mono.just(submission)
        every { checkResultRepository.save(checkResult) } returns Mono.just(savedResult)
        every { submissionService.update(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.saveAndUpdateSubmission(checkResult))
            .assertNext { (saved, sub) ->
                assertThat(saved.id).isEqualTo("cr-saved")
                assertThat(sub.id).isEqualTo("sub-1")
            }
            .verifyComplete()

        verify(exactly = 1) { submissionService.update(any()) }
    }

    @Test
    fun `saveAndUpdateSubmission emits NOT_FOUND when submission is missing`() {
        val checkResult = BackendFixtures.checkResult(submissionId = "missing")
        every { submissionService.findById("missing") } returns Mono.empty()

        StepVerifier.create(service.saveAndUpdateSubmission(checkResult))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.NOT_FOUND }
            .verify()

        verify(exactly = 0) { checkResultRepository.save(any()) }
    }

    // endregion

    // region findById

    @Test
    fun `findById delegates to repository`() {
        val result = BackendFixtures.checkResult(id = "cr-1")
        every { checkResultRepository.findById("cr-1") } returns Mono.just(result)

        StepVerifier.create(service.findById("cr-1")).expectNext(result).verifyComplete()
    }

    @Test
    fun `findById returns empty Mono when not found`() {
        every { checkResultRepository.findById("nonexistent") } returns Mono.empty()

        StepVerifier.create(service.findById("nonexistent")).verifyComplete()
    }

    // endregion

    // region findAllBySubmissionId

    @Test
    fun `findAllBySubmissionId delegates to repository sorted by createdAt DESC`() {
        val r1 = BackendFixtures.checkResult(id = "cr-1", submissionId = "sub-1")
        val r2 = BackendFixtures.checkResult(id = "cr-2", submissionId = "sub-1")
        val expectedSort = Sort.by(Sort.Direction.DESC, "createdAt")
        every { checkResultRepository.findBySubmissionId("sub-1", expectedSort) } returns Flux.just(r1, r2)

        StepVerifier.create(service.findAllBySubmissionId("sub-1")).expectNext(r1, r2).verifyComplete()
    }

    @Test
    fun `findAllBySubmissionId returns empty flux when no results exist`() {
        val expectedSort = Sort.by(Sort.Direction.DESC, "createdAt")
        every { checkResultRepository.findBySubmissionId("sub-99", expectedSort) } returns Flux.empty()

        StepVerifier.create(service.findAllBySubmissionId("sub-99")).verifyComplete()
    }

    // endregion
}
