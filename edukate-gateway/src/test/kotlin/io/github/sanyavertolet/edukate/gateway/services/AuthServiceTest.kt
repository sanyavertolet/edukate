package io.github.sanyavertolet.edukate.gateway.services

import io.github.sanyavertolet.edukate.auth.services.JwtTokenService
import io.github.sanyavertolet.edukate.gateway.GatewayFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class AuthServiceTest {
    private val userDetailsService: UserDetailsService = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val jwtTokenService: JwtTokenService = mockk()
    private val authService = AuthService(userDetailsService, passwordEncoder, jwtTokenService)

    private val userDetails = GatewayFixtures.edukateUserDetails(token = GatewayFixtures.ENCODED_PASSWORD)

    // ── signIn ────────────────────────────────────────────────────────────────

    @Test
    fun `signIn returns JWT when credentials are valid`() {
        every { userDetailsService.findEdukateUserDetailsByUsername(GatewayFixtures.USER_NAME) } returns
            Mono.just(userDetails)
        every { passwordEncoder.matches(GatewayFixtures.RAW_PASSWORD, GatewayFixtures.ENCODED_PASSWORD) } returns true
        every { jwtTokenService.generateToken(userDetails) } returns "jwt-token"

        StepVerifier.create(authService.signIn(GatewayFixtures.signInRequest())).expectNext("jwt-token").verifyComplete()
    }

    @Test
    fun `signIn returns empty Mono when password does not match`() {
        every { userDetailsService.findEdukateUserDetailsByUsername(GatewayFixtures.USER_NAME) } returns
            Mono.just(userDetails)
        every { passwordEncoder.matches(GatewayFixtures.RAW_PASSWORD, GatewayFixtures.ENCODED_PASSWORD) } returns false

        StepVerifier.create(authService.signIn(GatewayFixtures.signInRequest())).verifyComplete()
    }

    @Test
    fun `signIn returns empty Mono when user is not found`() {
        every { userDetailsService.findEdukateUserDetailsByUsername(GatewayFixtures.USER_NAME) } returns Mono.empty()

        StepVerifier.create(authService.signIn(GatewayFixtures.signInRequest())).verifyComplete()
    }

    // ── signUp ────────────────────────────────────────────────────────────────

    @Test
    fun `signUp returns JWT when username is available and user is created`() {
        val request = GatewayFixtures.signUpRequest()
        every { userDetailsService.isNotUserPresent(request.username) } returns Mono.just(true)
        every { passwordEncoder.encode(request.password) } returns GatewayFixtures.ENCODED_PASSWORD
        every { userDetailsService.create(request.username, request.email, GatewayFixtures.ENCODED_PASSWORD) } returns
            Mono.just(userDetails)
        every { jwtTokenService.generateToken(userDetails) } returns "jwt-token"

        StepVerifier.create(authService.signUp(request)).expectNext("jwt-token").verifyComplete()
    }

    @Test
    fun `signUp emits CONFLICT error when username already exists`() {
        val request = GatewayFixtures.signUpRequest()
        every { userDetailsService.isNotUserPresent(request.username) } returns Mono.just(false)

        StepVerifier.create(authService.signUp(request))
            .expectErrorSatisfies { error ->
                assertThat(error).isInstanceOf(ResponseStatusException::class.java)
                assertThat((error as ResponseStatusException).statusCode).isEqualTo(HttpStatus.CONFLICT)
            }
            .verify()
    }

    @Test
    fun `signUp encodes password before persisting`() {
        val request = GatewayFixtures.signUpRequest()
        every { userDetailsService.isNotUserPresent(request.username) } returns Mono.just(true)
        every { passwordEncoder.encode(request.password) } returns GatewayFixtures.ENCODED_PASSWORD
        every { userDetailsService.create(any(), any(), any()) } returns Mono.just(userDetails)
        every { jwtTokenService.generateToken(any()) } returns "jwt-token"

        StepVerifier.create(authService.signUp(request)).expectNext("jwt-token").verifyComplete()

        verify(exactly = 1) { passwordEncoder.encode(request.password) }
    }
}
