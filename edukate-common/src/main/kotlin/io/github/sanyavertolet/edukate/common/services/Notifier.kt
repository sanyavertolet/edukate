package io.github.sanyavertolet.edukate.common.services

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest
import reactor.core.publisher.Mono

interface Notifier {
    fun notify(notificationCreationRequest: BaseNotificationCreateRequest): Mono<String>
}
