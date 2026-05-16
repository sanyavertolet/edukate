package io.github.sanyavertolet.edukate.common.services

import io.github.sanyavertolet.edukate.common.notifications.BaseEmailMessage
import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Profile("notifier")
class RabbitEmailPublisher(private val rabbitTemplate: RabbitTemplate) : EmailPublisher {
    override fun publish(message: BaseEmailMessage): Mono<Void> =
        Mono.fromRunnable<Void> { rabbitTemplate.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.Rk.EMAIL, message) }
}
