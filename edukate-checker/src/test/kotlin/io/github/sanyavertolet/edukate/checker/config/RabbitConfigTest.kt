package io.github.sanyavertolet.edukate.checker.config

import io.github.sanyavertolet.edukate.checker.configs.RabbitConfig
import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange

class RabbitConfigTest {
    private val exchange = TopicExchange(RabbitTopology.EXCHANGE)
    private val config = RabbitConfig()
    private val declared = config.scheduleTopology(exchange)

    @Test
    fun `queue name matches topology constant`() {
        val queue = declared.getDeclarablesByType(Queue::class.java).first()
        assertThat(queue.name).isEqualTo(RabbitTopology.Q.SCHEDULE_CHECKER)
    }

    @Test
    fun `queue is durable`() {
        val queue = declared.getDeclarablesByType(Queue::class.java).first()
        assertThat(queue.isDurable).isTrue
    }

    @Test
    fun `binding routing key matches constant`() {
        val binding = declared.getDeclarablesByType(Binding::class.java).first()
        assertThat(binding.routingKey).isEqualTo(RabbitTopology.Rk.SCHEDULE)
    }

    @Test
    fun `binding exchange matches constant`() {
        val binding = declared.getDeclarablesByType(Binding::class.java).first()
        assertThat(binding.exchange).isEqualTo(RabbitTopology.EXCHANGE)
    }
}
