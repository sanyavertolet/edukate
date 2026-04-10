package io.github.sanyavertolet.edukate.backend.configs

import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Declarables
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {
    @Bean
    fun resultTopology(edukateExchange: TopicExchange): Declarables {
        val resultQueue = QueueBuilder.durable(RabbitTopology.Q.RESULT_BACKEND).build()
        val binding: Binding = BindingBuilder.bind(resultQueue).to(edukateExchange).with(RabbitTopology.Rk.RESULT)
        return Declarables(resultQueue, binding)
    }
}
