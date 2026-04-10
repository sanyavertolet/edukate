package io.github.sanyavertolet.edukate.checker.configs

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
    fun scheduleTopology(edukateExchange: TopicExchange): Declarables {
        val scheduleQueue = QueueBuilder.durable(RabbitTopology.Q.SCHEDULE_CHECKER).build()
        val binding = BindingBuilder.bind(scheduleQueue).to(edukateExchange).with(RabbitTopology.Rk.SCHEDULE)
        return Declarables(scheduleQueue, binding)
    }
}
