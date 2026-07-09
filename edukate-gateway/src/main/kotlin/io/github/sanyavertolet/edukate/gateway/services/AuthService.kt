package io.github.sanyavertolet.edukate.gateway.services

import io.github.sanyavertolet.edukate.auth.services.JwtTokenService
import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import io.github.sanyavertolet.edukate.common.users.UserStatus
import io.github.sanyavertolet.edukate.gateway.dtos.SignInRequest
import io.github.sanyavertolet.edukate.gateway.dtos.SignUpRequest
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class AuthService(
    private val userDetailsService: UserDetailsService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenService: JwtTokenService,
    private val backendService: BackendService,
) {
    fun signIn(signInRequest: SignInRequest): Mono<String> =
        userDetailsService
            .findEdukateUserDetailsByUsername(signInRequest.username)
            .filter { passwordEncoder.matches(signInRequest.password, it.password) }
            .flatMap { details ->
                when (details.status) {
                    UserStatus.PENDING ->
                        details.email
                            .let { email -> backendService.requestVerificationEmail(details.id, email) }
                            .onErrorResume { Mono.empty() }
                            .then(Mono.error(ResponseStatusException(HttpStatus.LOCKED, "Email not verified")))
                    else -> Mono.just(jwtTokenService.generateToken(details))
                }
            }

    fun resendVerification(email: String): Mono<Void> =
        backendService
            .getUserByEmail(email)
            .filter { it.status == UserStatus.PENDING }
            .flatMap { user -> backendService.requestVerificationEmail(requireNotNull(user.id), email) }
            .onErrorResume { Mono.empty() }

    fun signUp(signUpRequest: SignUpRequest): Mono<Void> =
        signUpRequest
            .toMono()
            .filterWhen { userDetailsService.isNotUserPresent(it.username) }
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.CONFLICT)))
            .flatMap { req ->
                userDetailsService
                    .create(req.username, req.email, checkNotNull(passwordEncoder.encode(req.password)))
                    .flatMap { details -> backendService.requestVerificationEmail(requireNotNull(details.id), req.email) }
            }

    fun verifyEmail(token: String): Mono<Void> = backendService.consumeVerificationToken(token)

    fun forgotPassword(email: String): Mono<Void> = backendService.requestPasswordReset(email).onErrorResume { Mono.empty() }

    fun resetPassword(token: String, newPassword: String): Mono<Void> =
        backendService.consumeResetToken(token, checkNotNull(passwordEncoder.encode(newPassword)))

    fun changeUsername(userId: Long, newUsername: String): Mono<String> =
        backendService.updateUserName(userId, newUsername).map { credentials ->
            jwtTokenService.generateToken(EdukateUserDetails(credentials))
        }

    fun changePassword(username: String, currentPassword: String, newPassword: String): Mono<Void> =
        userDetailsService
            .findEdukateUserDetailsByUsername(username)
            .filter { passwordEncoder.matches(currentPassword, it.password) }
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect")))
            .flatMap { backendService.updateUserPassword(it.id, checkNotNull(passwordEncoder.encode(newPassword))) }
}
