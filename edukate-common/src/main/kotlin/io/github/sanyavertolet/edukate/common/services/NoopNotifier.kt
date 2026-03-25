package io.github.sanyavertolet.edukate.common.services

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
@Profile("!notifier")
class NoopNotifier : Notifier {
    override fun notify(notificationCreationRequest: BaseNotificationCreateRequest): Mono<String> =
        "stub".toMono().also { logger.warn("Notification is meant to be send: {}", notificationCreationRequest) }

    companion object {
        private val logger = LoggerFactory.getLogger(NoopNotifier::class.java)
    }
}
