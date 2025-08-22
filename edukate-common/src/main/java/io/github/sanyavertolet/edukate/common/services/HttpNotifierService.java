package io.github.sanyavertolet.edukate.common.services;

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest;
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest;
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest;
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

    public Mono<String> notify(BaseNotificationCreateRequest notificationCreationRequest) {
        return client.post()
                .uri("/internal/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(notificationCreationRequest), BaseNotificationCreateRequest.class)
                .retrieve()
                .bodyToMono(String.class);
    }

    @SuppressWarnings("unused")
    public Mono<String> notifySimple(String targetUserId, String title, String message, String source) {
        return notify(SimpleNotificationCreateRequest.of(
                UUID.randomUUID().toString(), targetUserId, title, message, source
        ));
    }

    public Mono<String> notifyInvite(
            String targetUserId, String inviterName, String bundleName, String bundleShareCode
    ) {
        return notify(InviteNotificationCreateRequest.of(
                UUID.randomUUID().toString(), targetUserId, inviterName, bundleName, bundleShareCode
        ));
    }
}
