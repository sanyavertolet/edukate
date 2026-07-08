package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.AuthToken
import io.github.sanyavertolet.edukate.backend.entities.AuthTokenType
import io.github.sanyavertolet.edukate.backend.repositories.AuthTokenRepository
import io.github.sanyavertolet.edukate.common.notifications.EmailVerificationMessage
import io.github.sanyavertolet.edukate.common.notifications.PasswordResetMessage
import io.github.sanyavertolet.edukate.common.services.EmailPublisher
import io.github.sanyavertolet.edukate.common.users.UserStatus
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.github.sanyavertolet.edukate.common.utils.throwIf
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
        // Reject up front if the address already belongs to a *different* user, so the caller
        // gets an immediate 409 instead of a verification link that fails (or duplicates an
        // email) on consume. throwIf passes through when the email is unused (empty) or is the
        // requester's own — the latter matters because this method issues the sign-up token
        // too, where the email is the user's current one. The token work hangs off a trailing
        // flatMap, so it is built only once the guard passes — never on a rejected conflict.
        return userService
            .findUserByEmail(email)
            .throwIf(HttpStatus.CONFLICT, "Email '$email' is already in use") { owner -> owner.id != userId }
            .then(userService.findUserById(userId))
            .flatMap { user ->
                authTokenRepository
                    .deleteAllByUserIdAndType(userId, AuthTokenType.EMAIL_VERIFICATION)
                    .then(
                        authTokenRepository.save(
                            AuthToken(token, userId, AuthTokenType.EMAIL_VERIFICATION, expiresAt, email)
                        )
                    )
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
                            AuthToken(token, requireNotNull(user.id), AuthTokenType.PASSWORD_RESET, expiresAt, email)
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
            .orNotFound("Token not found")
            .flatMap { authToken ->
                if (authToken.expiresAt.isBefore(Instant.now())) {
                    authTokenRepository
                        .deleteById(token)
                        .then(Mono.error(ResponseStatusException(HttpStatus.GONE, "Token expired")))
                } else {
                    authTokenRepository
                        .deleteById(token)
                        .then(userService.findUserById(authToken.userId))
                        .flatMap { user ->
                            // Token always carries the email being verified.
                            // Sign-up: equals the user's current email (no-op).
                            // Email-change: the new address — overwrites users.email.
                            userService.saveUser(user.copy(status = UserStatus.ACTIVE, email = authToken.email))
                        }
                        .then()
                }
            }

    @Transactional
    fun consumeResetToken(token: UUID, encodedPassword: String): Mono<Void> =
        authTokenRepository.findByTokenAndType(token, AuthTokenType.PASSWORD_RESET).orNotFound("Token not found").flatMap {
            authToken ->
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
