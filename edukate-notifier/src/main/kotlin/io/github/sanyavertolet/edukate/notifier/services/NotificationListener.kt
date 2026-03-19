package io.github.sanyavertolet.edukate.notifier.services

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest
import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class NotificationListener(private val notificationService: NotificationService) {
    /**
     * Accepts any subclass of BaseNotificationCreationRequest and converts it to the appropriate entity.
     *
     * **Field `_type` is required for deserialization.**
     *
     * @param createRequest Notification creation request
     */
    @RabbitListener(queues = [RabbitTopology.Q.NOTIFY])
    fun scheduleCheck(createRequest: BaseNotificationCreateRequest) {
        log.debug("received notification request={}", createRequest.uuid)
        // TODO: avoid manual subscribe() in listener; wire explicit reactive lifecycle/error handling instead.
        notificationService.saveIfAbsent(createRequest).subscribe()
    }

    companion object {
        private val log = LoggerFactory.getLogger(NotificationListener::class.java)
    }
}
