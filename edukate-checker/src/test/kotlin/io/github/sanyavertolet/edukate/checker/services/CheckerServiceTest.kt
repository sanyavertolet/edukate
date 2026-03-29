@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.checker.services

import io.github.sanyavertolet.edukate.checker.CheckerFixtures
import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class CheckerServiceTest {
    private val chatService = mockk<ChatService>()
    private val resolver = mockk<MediaContentResolver>()
    private lateinit var service: CheckerService

    private val ctx = CheckerFixtures.submissionContext()
    private val problemMedia = listOf(CheckerFixtures.mockMedia())
    private val submissionMedia = listOf(CheckerFixtures.mockMedia())

    @BeforeEach
    fun setUp() {
        service = CheckerService(chatService, resolver)
        every { resolver.resolveMedia(ctx.problemImageRawKeys) } returns Flux.fromIterable(problemMedia)
        every { resolver.resolveMedia(ctx.submissionImageRawKeys) } returns Flux.fromIterable(submissionMedia)
    }

    @Test
    fun `happy path returns SUCCESS result`() {
        val successResponse = CheckerFixtures.modelResponse(status = CheckStatus.SUCCESS)
        every { chatService.makeRequest(any()) } returns Mono.just(successResponse)

        StepVerifier.create(service.runCheck(ctx))
            .assertNext { result ->
                assertThat(result.status).isEqualTo(CheckStatus.SUCCESS)
                assertThat(result.submissionId).isEqualTo(ctx.submissionId)
            }
            .verifyComplete()
    }

    @Test
    fun `problem text is forwarded to RequestContext`() {
        val requestContextSlot = slot<io.github.sanyavertolet.edukate.checker.domain.RequestContext>()
        every { chatService.makeRequest(capture(requestContextSlot)) } returns Mono.just(CheckerFixtures.modelResponse())

        StepVerifier.create(service.runCheck(ctx)).expectNextCount(1).verifyComplete()

        assertThat(requestContextSlot.captured.problemText).isEqualTo(ctx.problemText)
    }

    @Test
    fun `resolved images are passed to RequestContext`() {
        val requestContextSlot = slot<io.github.sanyavertolet.edukate.checker.domain.RequestContext>()
        every { chatService.makeRequest(capture(requestContextSlot)) } returns Mono.just(CheckerFixtures.modelResponse())

        StepVerifier.create(service.runCheck(ctx)).expectNextCount(1).verifyComplete()

        assertThat(requestContextSlot.captured.submissionImages).isEqualTo(submissionMedia)
        assertThat(requestContextSlot.captured.problemImages).isEqualTo(problemMedia)
    }

    @Test
    fun `MISTAKE response is mapped correctly`() {
        val response = ModelResponse(CheckStatus.MISTAKE, 0.6f, CheckErrorType.CONCEPTUAL, "Conceptual error")
        every { chatService.makeRequest(any()) } returns Mono.just(response)

        StepVerifier.create(service.runCheck(ctx))
            .assertNext { result ->
                assertThat(result.status).isEqualTo(CheckStatus.MISTAKE)
                assertThat(result.errorType).isEqualTo(CheckErrorType.CONCEPTUAL)
                assertThat(result.trustLevel).isEqualTo(0.6f)
            }
            .verifyComplete()
    }

    @Test
    fun `chatService error returns INTERNAL_ERROR`() {
        every { chatService.makeRequest(any()) } returns Mono.error(RuntimeException("AI unavailable"))

        StepVerifier.create(service.runCheck(ctx))
            .assertNext { result ->
                assertThat(result.status).isEqualTo(CheckStatus.INTERNAL_ERROR)
            }
            .verifyComplete()
    }

    @Test
    fun `resolver error returns INTERNAL_ERROR`() {
        every { resolver.resolveMedia(ctx.submissionImageRawKeys) } returns Flux.error(RuntimeException("S3 unavailable"))

        StepVerifier.create(service.runCheck(ctx))
            .assertNext { result ->
                assertThat(result.status).isEqualTo(CheckStatus.INTERNAL_ERROR)
            }
            .verifyComplete()
    }

    @Test
    fun `empty chatService response returns INTERNAL_ERROR`() {
        every { chatService.makeRequest(any()) } returns Mono.empty()

        StepVerifier.create(service.runCheck(ctx))
            .assertNext { result ->
                assertThat(result.status).isEqualTo(CheckStatus.INTERNAL_ERROR)
            }
            .verifyComplete()
    }
}
