package io.github.sanyavertolet.edukate.notifier.configs

import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Declarables
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {
    @Bean
    fun notifyTopology(edukateExchange: TopicExchange): Declarables {
        val notifyQueue = QueueBuilder.durable(RabbitTopology.Q.NOTIFY).build()
        val binding = BindingBuilder.bind(notifyQueue).to(edukateExchange).with(RabbitTopology.Rk.NOTIFY)
        return Declarables(notifyQueue, binding)
    }
}
