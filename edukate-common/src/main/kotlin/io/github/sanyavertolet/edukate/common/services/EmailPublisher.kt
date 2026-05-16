package io.github.sanyavertolet.edukate.common.services

import io.github.sanyavertolet.edukate.common.notifications.BaseEmailMessage
import reactor.core.publisher.Mono

fun interface EmailPublisher {
    fun publish(message: BaseEmailMessage): Mono<Void>
}
