package io.github.sanyavertolet.edukate.common.services;

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Profile("notifier")
@RequiredArgsConstructor
@ConditionalOnMissingBean(Notifier.class)
public class HttpNotifier implements Notifier {
    @Value("${notifier.url}")
    private String notifierUrl;

    private WebClient client;

    @PostConstruct
    private void init() {
        client = WebClient.create(notifierUrl);
    }

    @Override
    public Mono<String> notify(BaseNotificationCreateRequest notificationCreationRequest) {
        return client.post()
                .uri("/internal/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(notificationCreationRequest)
                .retrieve()
                .bodyToMono(String.class);
    }
}
