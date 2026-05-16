package io.github.sanyavertolet.edukate.common.services

import io.github.sanyavertolet.edukate.common.notifications.BaseEmailMessage
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Profile("!notifier")
class NoopEmailPublisher : EmailPublisher {
    override fun publish(message: BaseEmailMessage): Mono<Void> =
        Mono.empty<Void>().also { logger.warn("Email message suppressed (no notifier profile): {}", message) }

    companion object {
        private val logger = LoggerFactory.getLogger(NoopEmailPublisher::class.java)
    }
}
