package io.github.sanyavertolet.edukate.gateway.services

import io.github.sanyavertolet.edukate.common.users.UserCredentials
import io.github.sanyavertolet.edukate.gateway.configs.GatewayProperties
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class BackendService(gatewayProperties: GatewayProperties, webClientBuilder: WebClient.Builder) {
    private val webClient: WebClient = webClientBuilder.baseUrl(gatewayProperties.backend.url).build()

    fun saveUser(userCredentials: UserCredentials): Mono<UserCredentials> =
        webClient.post().uri("/internal/users").bodyValue(userCredentials).retrieve().bodyToMono<UserCredentials>()

    fun getUserByName(name: String): Mono<UserCredentials> =
        webClient.get().uri("/internal/users/by-name/{name}", name).retrieve().bodyToMono<UserCredentials>().onErrorResume(
            WebClientResponseException.NotFound::class.java
        ) {
            Mono.empty()
        }

    @Cacheable(cacheNames = ["user-credentials-by-id"], key = "#id")
    fun getUserById(id: String): Mono<UserCredentials> =
        webClient
            .get()
            .uri("/internal/users/by-id/{id}", id)
            .retrieve()
            .bodyToMono<UserCredentials>()
            .map { it.copy(encodedPassword = "") }
            .onErrorResume(WebClientResponseException.NotFound::class.java) { Mono.empty() }
}
