@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.checker.components

import io.github.sanyavertolet.edukate.checker.CheckerFixtures
import io.github.sanyavertolet.edukate.checker.services.CheckerService
import io.github.sanyavertolet.edukate.checker.services.ResultPublisher
import io.github.sanyavertolet.edukate.checker.services.SubmissionContextListener
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class SubmissionContextListenerTest {
    private val checkerService = mockk<CheckerService>()
    private val resultPublisher = mockk<ResultPublisher>()
    private lateinit var listener: SubmissionContextListener

    private val ctx = CheckerFixtures.submissionContext()
    private val result = CheckerFixtures.checkResultMessage()

    @BeforeEach
    fun setUp() {
        listener = SubmissionContextListener(checkerService, resultPublisher)
    }

    @Test
    fun `onSubmissionContext delegates to CheckerService`() {
        every { checkerService.runCheck(ctx) } returns Mono.just(result)
        every { resultPublisher.publish(result) } returns Mono.empty()

        listener.onSubmissionContext(ctx)

        verify { checkerService.runCheck(ctx) }
    }

    @Test
    fun `result is published`() {
        every { checkerService.runCheck(ctx) } returns Mono.just(result)
        every { resultPublisher.publish(result) } returns Mono.empty()

        listener.onSubmissionContext(ctx)

        verify { resultPublisher.publish(result) }
    }

    @Test
    fun `method returns without hanging`() {
        every { checkerService.runCheck(ctx) } returns Mono.just(result)
        every { resultPublisher.publish(result) } returns Mono.empty()

        // If .block() hangs, the test times out; completing without timeout validates the chain
        listener.onSubmissionContext(ctx)
    }

    @Test
    fun `CheckerService error propagates via block`() {
        // Note: CheckerService.runCheck() always uses onErrorReturn so it never errors in production.
        // When directly mocked to Mono.error(), the .block() in the listener will throw rather than
        // swallow — this test documents that actual behavior.
        every { checkerService.runCheck(ctx) } returns Mono.error(RuntimeException("check failed"))

        assertThatThrownBy { listener.onSubmissionContext(ctx) }
            .isInstanceOf(RuntimeException::class.java)
    }
}
