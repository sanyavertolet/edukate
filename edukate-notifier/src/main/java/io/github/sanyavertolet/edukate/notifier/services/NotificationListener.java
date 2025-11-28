package io.github.sanyavertolet.edukate.notifier.services;

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest;
import io.github.sanyavertolet.edukate.messaging.RabbitTopology;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class NotificationListener {
     private final NotificationService notificationService;

    /**
     * Accepts any subclass of BaseNotificationCreationRequest and converts it to the appropriate entity.
     * <p>
     * <b>Field `_type` is required for deserialization.</b>
     *
     * @param createRequest Notification creation request
     */
    @RabbitListener(queues = RabbitTopology.Q_NOTIFY)
    public void scheduleCheck(BaseNotificationCreateRequest createRequest) {
        log.debug("received notification request={}", createRequest.getUuid());
        notificationService.saveIfAbsent(createRequest).subscribe();
    }
}
