package io.github.sanyavertolet.edukate.gateway.services

import io.github.sanyavertolet.edukate.common.users.UserCredentials
import io.github.sanyavertolet.edukate.gateway.configs.GatewayProperties
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class BackendService(private val gatewayProperties: GatewayProperties) {
    private lateinit var webClient: WebClient

    @PostConstruct
    fun init() {
        webClient = WebClient.create(gatewayProperties.backend.url)
    }

    fun saveUser(userCredentials: UserCredentials): Mono<UserCredentials> =
        webClient.post().uri("/internal/users").bodyValue(userCredentials).retrieve().bodyToMono<UserCredentials>()

    fun getUserByName(name: String): Mono<UserCredentials> =
        webClient.get().uri("/internal/users/by-name/{name}", name).retrieve().bodyToMono<UserCredentials>()

    fun getUserById(id: String): Mono<UserCredentials> =
        webClient.get().uri("/internal/users/by-id/{id}", id).retrieve().bodyToMono<UserCredentials>()
}
