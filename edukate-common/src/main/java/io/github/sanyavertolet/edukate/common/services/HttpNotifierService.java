package io.github.sanyavertolet.edukate.common.services;

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreationRequest;
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreationRequest;
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreationRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Profile("notifier")
@Service
@RequiredArgsConstructor
public class HttpNotifierService {
    @Value("${notifier.url}")
    private String notifierUrl;

    private WebClient client;

    @PostConstruct
    private void init() {
        client = WebClient.create(notifierUrl);
    }

    public Mono<String> notify(BaseNotificationCreationRequest notificationCreationRequest) {
        return client.post()
                .uri("/internal/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(notificationCreationRequest), BaseNotificationCreationRequest.class)
                .retrieve()
                .bodyToMono(String.class);
    }

    @SuppressWarnings("unused")
    public Mono<String> notifySimple(String targetUserId, String title, String message, String source) {
        return notify(SimpleNotificationCreationRequest.of(
                UUID.randomUUID().toString(), targetUserId, title, message, source
        ));
    }

    public Mono<String> notifyInvite(
            String targetUserId, String inviterName, String bundleName, String bundleShareCode
    ) {
        return notify(InviteNotificationCreationRequest.of(
                UUID.randomUUID().toString(), targetUserId, inviterName, bundleName, bundleShareCode
        ));
    }
}
