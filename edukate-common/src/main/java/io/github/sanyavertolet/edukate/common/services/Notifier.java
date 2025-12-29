package io.github.sanyavertolet.edukate.common.services;

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest;
import reactor.core.publisher.Mono;

public interface Notifier {
    Mono<String> notify(BaseNotificationCreateRequest notificationCreationRequest);
}
