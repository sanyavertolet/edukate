package io.github.sanyavertolet.edukate.common.services;

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest;
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface Notifier {
    Mono<String> notify(BaseNotificationCreateRequest notificationCreationRequest);

    default Mono<String> notifyInvite(
            String targetUserId, String inviterName, String bundleName, String bundleShareCode
    ) {
        var notification = InviteNotificationCreateRequest.builder()
                .uuid(UUID.randomUUID().toString())
                .targetUserId(targetUserId)
                .inviterName(inviterName)
                .bundleName(bundleName)
                .bundleShareCode(bundleShareCode)
                .build();
        return notify(notification);
    }
}
