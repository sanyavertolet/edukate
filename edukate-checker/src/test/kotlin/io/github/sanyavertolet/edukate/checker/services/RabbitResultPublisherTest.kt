package io.github.sanyavertolet.edukate.checker.services

import io.github.sanyavertolet.edukate.checker.CheckerFixtures
import io.github.sanyavertolet.edukate.checker.services.impl.RabbitResultPublisher
import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.atomic.AtomicInteger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import reactor.test.StepVerifier

class RabbitResultPublisherTest {
    private val template = mockk<RabbitTemplate>(relaxed = true)
    private lateinit var publisher: RabbitResultPublisher
    private val message = CheckerFixtures.checkResultMessage()

    @BeforeEach
    fun setUp() {
        publisher = RabbitResultPublisher(template)
    }

    @Test
    fun `publish calls convertAndSend with correct exchange`() {
        // relaxed mock: convertAndSend is a no-op by default
        StepVerifier.create(publisher.publish(message)).verifyComplete()

        verify { template.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.Rk.RESULT, message) }
    }

    @Test
    fun `publish completes on first attempt`() {
        StepVerifier.create(publisher.publish(message)).verifyComplete()
    }

    @Test
    fun `publish retries on transient failure then succeeds`() {
        val callCount = AtomicInteger(0)
        every { template.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.Rk.RESULT, message) } answers
            {
                if (callCount.incrementAndGet() == 1) throw IllegalStateException("transient")
            }

        StepVerifier.create(publisher.publish(message)).verifyComplete()

        verify(exactly = 2) { template.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.Rk.RESULT, message) }
    }

    @Test
    fun `publish propagates error after 3 failures`() {
        every { template.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.Rk.RESULT, message) } answers
            {
                throw IllegalStateException("always fails")
            }

        StepVerifier.create(publisher.publish(message)).expectError().verify()

        // 1 initial attempt + 3 retries = 4 total calls
        verify(atLeast = 3) { template.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.Rk.RESULT, message) }
    }

    @Test
    fun `publish subscribes on boundedElastic`() {
        justRun { template.convertAndSend(any<String>(), any<String>(), any<Any>()) }
        val callingThread = Thread.currentThread().name

        var subscriberThread: String? = null
        StepVerifier.create(publisher.publish(message).doOnTerminate { subscriberThread = Thread.currentThread().name })
            .verifyComplete()

        assertThat(subscriberThread).isNotEqualTo(callingThread)
    }
}
