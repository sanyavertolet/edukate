package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.AuthToken
import io.github.sanyavertolet.edukate.backend.entities.AuthTokenType
import io.github.sanyavertolet.edukate.backend.repositories.AuthTokenRepository
import io.github.sanyavertolet.edukate.common.notifications.EmailVerificationMessage
import io.github.sanyavertolet.edukate.common.notifications.PasswordResetMessage
import io.github.sanyavertolet.edukate.common.services.EmailPublisher
import io.github.sanyavertolet.edukate.common.users.UserStatus
import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Service
class AuthTokenService(
    private val authTokenRepository: AuthTokenRepository,
    private val userService: UserService,
    private val emailPublisher: EmailPublisher,
    @param:Value("\${edukate.auth.verification-ttl-seconds:86400}") private val verificationTtlSeconds: Long,
    @param:Value("\${edukate.auth.password-reset-ttl-seconds:3600}") private val passwordResetTtlSeconds: Long,
) {
    fun issueVerificationToken(userId: Long, email: String): Mono<Void> {
        val token = UUID.randomUUID()
        val expiresAt = Instant.now().plus(Duration.ofSeconds(verificationTtlSeconds))
        return userService.findUserById(userId).flatMap { user ->
            authTokenRepository
                .deleteAllByUserIdAndType(userId, AuthTokenType.EMAIL_VERIFICATION)
                .then(authTokenRepository.save(AuthToken(token, userId, AuthTokenType.EMAIL_VERIFICATION, expiresAt)))
                .flatMap { emailPublisher.publish(EmailVerificationMessage(email, token, user.name)) }
        }
    }

    fun issuePasswordResetToken(email: String): Mono<Void> {
        val token = UUID.randomUUID()
        val expiresAt = Instant.now().plus(Duration.ofSeconds(passwordResetTtlSeconds))
        return userService
            .findUserByEmail(email)
            .flatMap { user ->
                authTokenRepository
                    .deleteAllByUserIdAndType(requireNotNull(user.id), AuthTokenType.PASSWORD_RESET)
                    .then(
                        authTokenRepository.save(
                            AuthToken(token, requireNotNull(user.id), AuthTokenType.PASSWORD_RESET, expiresAt)
                        )
                    )
                    .flatMap { emailPublisher.publish(PasswordResetMessage(email, token, user.name)) }
            }
            .onErrorResume { Mono.empty() }
    }

    @Transactional
    fun consumeVerificationToken(token: UUID): Mono<Void> =
        authTokenRepository
            .findByTokenAndType(token, AuthTokenType.EMAIL_VERIFICATION)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found")))
            .flatMap { authToken ->
                if (authToken.expiresAt.isBefore(Instant.now())) {
                    authTokenRepository
                        .deleteById(token)
                        .then(Mono.error(ResponseStatusException(HttpStatus.GONE, "Token expired")))
                } else {
                    authTokenRepository
                        .deleteById(token)
                        .then(userService.findUserById(authToken.userId))
                        .flatMap { user -> userService.saveUser(user.copy(status = UserStatus.ACTIVE)) }
                        .then()
                }
            }

    @Transactional
    fun consumeResetToken(token: UUID, encodedPassword: String): Mono<Void> =
        authTokenRepository
            .findByTokenAndType(token, AuthTokenType.PASSWORD_RESET)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found")))
            .flatMap { authToken ->
                if (authToken.expiresAt.isBefore(Instant.now())) {
                    authTokenRepository
                        .deleteById(token)
                        .then(Mono.error(ResponseStatusException(HttpStatus.GONE, "Token expired")))
                } else {
                    authTokenRepository
                        .deleteById(token)
                        .then(userService.findUserById(authToken.userId))
                        .flatMap { user -> userService.saveUser(user.copy(token = encodedPassword)) }
                        .then()
                }
            }

    @Scheduled(fixedDelay = 3_600_000)
    fun cleanupExpiredTokens() {
        authTokenRepository.deleteAllByExpiresAtBefore(Instant.now()).subscribe()
    }
}
