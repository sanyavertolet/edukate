package io.github.sanyavertolet.edukate.notifier.controllers.internal;

import io.github.sanyavertolet.edukate.notifier.dtos.BaseNotificationDto;
import io.github.sanyavertolet.edukate.notifier.entities.BaseNotification;
import io.github.sanyavertolet.edukate.notifier.entities.SimpleNotification;
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
     * Accepts any subclass of BaseNotificationDto and converts it to the appropriate entity.
     * <p>
     * <b>Field `_type` is required for deserialization.</b>
     *
     * @param notificationDto The notification DTO to process
     * @return A Mono with the saved notification entity
     */
    @PostMapping
    public Mono<BaseNotification> postNotification(@RequestBody BaseNotificationDto notificationDto) {
        log.info("Received notification request: {}", notificationDto);
        return Mono.just(notificationDto)
                .map(BaseNotification::fromDto)
                .flatMap(notificationService::saveIfAbsent)
                .doOnSuccess(notification -> log.info("Successfully saved notification: {}", notification))
                .doOnError(e -> log.error("Error saving notification: {}", notificationDto, e));
    }

    @PostMapping("/simple")
    public Mono<BaseNotification> postSimpleNotification(
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam String source
    ) {
        return Mono.fromCallable(() -> new SimpleNotification(userId, title, message, source))
                .flatMap(notificationService::saveIfAbsent);
    }
}
