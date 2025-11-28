package io.github.sanyavertolet.edukate.common.services;

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Profile("!notifier")
@AllArgsConstructor
public class NoopNotifier implements Notifier {

    @Override
    public Mono<String> notify(BaseNotificationCreateRequest notificationCreationRequest) {
        log.warn("Notification is meant to be send: {}", notificationCreationRequest);
        return Mono.just("stub");
    }
}
