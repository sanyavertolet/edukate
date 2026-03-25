package io.github.sanyavertolet.edukate.common.services

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest
import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
@Profile("notifier")
class RabbitNotifier(private val rabbitTemplate: RabbitTemplate) : Notifier {

    override fun notify(notificationCreationRequest: BaseNotificationCreateRequest): Mono<String> {
        rabbitTemplate.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.Rk.NOTIFY, notificationCreationRequest)
        return notificationCreationRequest.uuid.toMono()
    }
}
