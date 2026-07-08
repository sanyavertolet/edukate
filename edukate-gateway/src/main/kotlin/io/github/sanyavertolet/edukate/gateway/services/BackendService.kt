package io.github.sanyavertolet.edukate.gateway.services

import io.github.sanyavertolet.edukate.common.users.UserCredentials
import io.github.sanyavertolet.edukate.gateway.configs.GatewayProperties
import io.github.sanyavertolet.edukate.gateway.repositories.GatewayUserRepository
import io.netty.channel.ChannelOption
import java.time.Duration
import org.springframework.cache.annotation.CacheEvict
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

    @CacheEvict(cacheNames = ["user-credentials-by-id"], key = "#userId")
    fun updateUserName(userId: Long, newName: String): Mono<UserCredentials> =
        webClient
            .patch()
            .uri("/internal/users/by-id/{id}/name", userId)
            .bodyValue(mapOf("newName" to newName))
            .retrieve()
            .bodyToMono<UserCredentials>()

    @CacheEvict(cacheNames = ["user-credentials-by-id"], key = "#userId")
    fun updateUserPassword(userId: Long, encodedPassword: String): Mono<Void> =
        webClient
            .patch()
            .uri("/internal/users/by-id/{id}/password", userId)
            .bodyValue(mapOf("encodedPassword" to encodedPassword))
            .retrieve()
            .bodyToMono<Void>()

    fun requestVerificationEmail(userId: Long, email: String): Mono<Void> =
        webClient
            .post()
            .uri("/internal/tokens/verification")
            .bodyValue(mapOf("userId" to userId.toString(), "email" to email))
            .retrieve()
            .bodyToMono<Void>()

    fun requestPasswordReset(email: String): Mono<Void> =
        webClient
            .post()
            .uri("/internal/tokens/password-reset")
            .bodyValue(mapOf("email" to email))
            .retrieve()
            .bodyToMono<Void>()

    fun consumeVerificationToken(token: String): Mono<Void> =
        webClient
            .post()
            .uri("/internal/tokens/consume/verification")
            .bodyValue(mapOf("token" to token))
            .retrieve()
            .bodyToMono<Void>()

    fun consumeResetToken(token: String, encodedPassword: String): Mono<Void> =
        webClient
            .post()
            .uri("/internal/tokens/consume/reset")
            .bodyValue(mapOf("token" to token, "encodedPassword" to encodedPassword))
            .retrieve()
            .bodyToMono<Void>()

    fun getUserByName(name: String): Mono<UserCredentials> =
        gatewayUserRepository.findByName(name).map { it.toCredentials() }

    fun getUserByEmail(email: String): Mono<UserCredentials> =
        gatewayUserRepository.findByEmail(email).map { it.toCredentials() }

    @Cacheable(cacheNames = ["user-credentials-by-id"], key = "#id")
    fun getUserById(id: Long): Mono<UserCredentials> =
        gatewayUserRepository.findById(id).map { it.toCredentials().copy(encodedPassword = "") }

    companion object {
        private const val CONNECT_TIMEOUT_MS = 3_000
        private const val RESPONSE_TIMEOUT_SECONDS = 10L
    }
}
