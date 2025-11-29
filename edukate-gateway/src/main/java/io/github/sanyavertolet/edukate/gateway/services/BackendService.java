package io.github.sanyavertolet.edukate.gateway.services;

import io.github.sanyavertolet.edukate.common.users.UserCredentials;
import io.github.sanyavertolet.edukate.gateway.configs.GatewayProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BackendService {
    private final GatewayProperties gatewayProperties;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.create(gatewayProperties.getBackend().url());
    }

    public Mono<UserCredentials> saveUser(UserCredentials userCredentials) {
        return webClient.post()
                .uri("/internal/users")
                .bodyValue(userCredentials)
                .retrieve()
                .bodyToMono(UserCredentials.class);
    }

    public Mono<UserCredentials> getUserByName(String name) {
        return webClient.get()
                .uri("/internal/users/by-name/{name}", name)
                .retrieve()
                .bodyToMono(UserCredentials.class);
    }

    public Mono<UserCredentials> getUserById(String id) {
        return webClient.get()
                .uri("/internal/users/by-id/{id}", id)
                .retrieve()
                .bodyToMono(UserCredentials.class);
    }
}
