package io.github.sanyavertolet.edukate.notifier.configs

import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Declarables
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitEmailConfig {
    @Bean
    fun emailTopology(edukateExchange: TopicExchange): Declarables {
        val emailQueue = QueueBuilder.durable(RabbitTopology.Q.EMAIL).build()
        val binding = BindingBuilder.bind(emailQueue).to(edukateExchange).with(RabbitTopology.Rk.EMAIL)
        return Declarables(emailQueue, binding)
    }
}
