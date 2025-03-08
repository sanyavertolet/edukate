package io.github.sanyavertolet.edukate.gateway.services;

import io.github.sanyavertolet.edukate.common.entities.User;
import io.github.sanyavertolet.edukate.gateway.configs.ConfigurationProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BackendService {
    private final ConfigurationProperties configurationProperties;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.create(configurationProperties.getBackend().getUrl());
    }

    public Mono<User> saveUser(User user) {
        return webClient.post()
                .uri("/internal/users")
                .body(Mono.just(user), User.class)
                .retrieve()
                .bodyToMono(User.class);
    }

    public Mono<User> getUserByName(String name) {
        return webClient.get()
                .uri("/internal/users/by-name/{name}", name)
                .retrieve()
                .bodyToMono(User.class);
    }
}
