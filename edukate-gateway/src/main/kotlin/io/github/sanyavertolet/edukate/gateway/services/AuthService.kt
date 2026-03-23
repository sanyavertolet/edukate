package io.github.sanyavertolet.edukate.gateway.services

import io.github.sanyavertolet.edukate.auth.services.JwtTokenService
import io.github.sanyavertolet.edukate.gateway.dtos.SignInRequest
import io.github.sanyavertolet.edukate.gateway.dtos.SignUpRequest
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Service
class AuthService(
    private val userDetailsService: UserDetailsService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenService: JwtTokenService,
) {
    fun signIn(signInRequest: SignInRequest): Mono<String> =
        userDetailsService
            .findEdukateUserDetailsByUsername(signInRequest.username)
            .filter { passwordEncoder.matches(signInRequest.password, it.password) }
            .map(jwtTokenService::generateToken)

    fun signUp(signUpRequest: SignUpRequest): Mono<String> =
        Mono.just(signUpRequest)
            .filterWhen { userDetailsService.isNotUserPresent(it.username) }
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.CONFLICT)))
            .flatMap { userDetailsService.create(it.username, it.email, passwordEncoder.encode(it.password)) }
            .map(jwtTokenService::generateToken)
}
