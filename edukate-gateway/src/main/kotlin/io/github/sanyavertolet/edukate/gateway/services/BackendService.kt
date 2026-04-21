package io.github.sanyavertolet.edukate.gateway.services

import io.github.sanyavertolet.edukate.common.users.UserCredentials
import io.github.sanyavertolet.edukate.gateway.configs.GatewayProperties
import io.github.sanyavertolet.edukate.gateway.repositories.GatewayUserRepository
import io.netty.channel.ChannelOption
import java.time.Duration
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient

@Service
class BackendService(
    gatewayProperties: GatewayProperties,
    webClientBuilder: WebClient.Builder,
    private val gatewayUserRepository: GatewayUserRepository,
) {
    private val webClient: WebClient =
        webClientBuilder
            .baseUrl(gatewayProperties.backend.url)
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                        .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
                )
            )
            .build()

    fun saveUser(userCredentials: UserCredentials): Mono<UserCredentials> =
        webClient.post().uri("/internal/users").bodyValue(userCredentials).retrieve().bodyToMono<UserCredentials>()

    fun getUserByName(name: String): Mono<UserCredentials> =
        gatewayUserRepository.findByName(name).map { it.toCredentials() }

    @Cacheable(cacheNames = ["user-credentials-by-id"], key = "#id")
    fun getUserById(id: Long): Mono<UserCredentials> =
        gatewayUserRepository.findById(id).map { it.toCredentials().copy(encodedPassword = "") }

    companion object {
        private const val CONNECT_TIMEOUT_MS = 3_000
        private const val RESPONSE_TIMEOUT_SECONDS = 10L
    }
}
