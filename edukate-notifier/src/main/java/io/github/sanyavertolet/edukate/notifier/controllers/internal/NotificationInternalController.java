package io.github.sanyavertolet.edukate.notifier.controllers.internal;

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreationRequest;
import io.github.sanyavertolet.edukate.notifier.entities.BaseNotification;
import io.github.sanyavertolet.edukate.notifier.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/notifications")
public class NotificationInternalController {
    private final NotificationService notificationService;

    /**
     * Endpoint for other services to post notifications.
     * Accepts any subclass of BaseNotificationCreationRequest and converts it to the appropriate entity.
     * <p>
     * <b>Field `_type` is required for deserialization.</b>
     *
     * @param creationRequest Notification creation request
     * @return A Mono with notification UUID as String
     */
    @PostMapping
    public Mono<String> postNotification(@RequestBody BaseNotificationCreationRequest creationRequest) {
        log.info("Received notification request: {}", creationRequest);
        return Mono.just(creationRequest)
                .map(BaseNotification::fromCreationRequest)
                .flatMap(notificationService::saveIfAbsent)
                .doOnSuccess(notification -> log.info("Successfully saved notification: {}", notification))
                .doOnError(e -> log.error("Error saving notification: {}", creationRequest, e))
                .map(BaseNotification::getUuid);
    }
}
