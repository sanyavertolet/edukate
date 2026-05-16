@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.gateway.services

import io.github.sanyavertolet.edukate.auth.services.JwtTokenService
import io.github.sanyavertolet.edukate.common.users.UserStatus
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
    private val backendService: BackendService = mockk()
    private val authService = AuthService(userDetailsService, passwordEncoder, jwtTokenService, backendService)

    private val activeUserDetails =
        GatewayFixtures.edukateUserDetails(token = GatewayFixtures.ENCODED_PASSWORD, status = UserStatus.ACTIVE)
    private val pendingUserDetails =
        GatewayFixtures.edukateUserDetails(
            token = GatewayFixtures.ENCODED_PASSWORD,
            status = UserStatus.PENDING,
            email = GatewayFixtures.USER_EMAIL,
        )

    // ── signIn — ACTIVE user ──────────────────────────────────────────────────

    @Test
    fun `signIn returns JWT when credentials are valid and user is ACTIVE`() {
        every { userDetailsService.findEdukateUserDetailsByUsername(GatewayFixtures.USER_NAME) } returns
            Mono.just(activeUserDetails)
        every { passwordEncoder.matches(GatewayFixtures.RAW_PASSWORD, GatewayFixtures.ENCODED_PASSWORD) } returns true
        every { jwtTokenService.generateToken(activeUserDetails) } returns "jwt-token"

        StepVerifier.create(authService.signIn(GatewayFixtures.signInRequest())).expectNext("jwt-token").verifyComplete()
    }

    @Test
    fun `signIn returns empty Mono when password does not match`() {
        every { userDetailsService.findEdukateUserDetailsByUsername(GatewayFixtures.USER_NAME) } returns
            Mono.just(activeUserDetails)
        every { passwordEncoder.matches(GatewayFixtures.RAW_PASSWORD, GatewayFixtures.ENCODED_PASSWORD) } returns false

        StepVerifier.create(authService.signIn(GatewayFixtures.signInRequest())).verifyComplete()
    }

    @Test
    fun `signIn returns empty Mono when user is not found`() {
        every { userDetailsService.findEdukateUserDetailsByUsername(GatewayFixtures.USER_NAME) } returns Mono.empty()

        StepVerifier.create(authService.signIn(GatewayFixtures.signInRequest())).verifyComplete()
    }

    // ── signIn — PENDING user ─────────────────────────────────────────────────

    @Test
    fun `signIn returns LOCKED error when user is PENDING and password is correct`() {
        every { userDetailsService.findEdukateUserDetailsByUsername(GatewayFixtures.USER_NAME) } returns
            Mono.just(pendingUserDetails)
        every { passwordEncoder.matches(GatewayFixtures.RAW_PASSWORD, GatewayFixtures.ENCODED_PASSWORD) } returns true
        every { backendService.requestVerificationEmail(GatewayFixtures.USER_ID, GatewayFixtures.USER_EMAIL) } returns
            Mono.empty()

        StepVerifier.create(authService.signIn(GatewayFixtures.signInRequest()))
            .expectErrorSatisfies { error ->
                assertThat(error).isInstanceOf(ResponseStatusException::class.java)
                assertThat((error as ResponseStatusException).statusCode).isEqualTo(HttpStatus.LOCKED)
            }
            .verify()
    }

    @Test
    fun `signIn auto-resends verification email when user is PENDING`() {
        every { userDetailsService.findEdukateUserDetailsByUsername(GatewayFixtures.USER_NAME) } returns
            Mono.just(pendingUserDetails)
        every { passwordEncoder.matches(GatewayFixtures.RAW_PASSWORD, GatewayFixtures.ENCODED_PASSWORD) } returns true
        every { backendService.requestVerificationEmail(GatewayFixtures.USER_ID, GatewayFixtures.USER_EMAIL) } returns
            Mono.empty()

        StepVerifier.create(authService.signIn(GatewayFixtures.signInRequest())).expectError().verify()

        verify(exactly = 1) { backendService.requestVerificationEmail(GatewayFixtures.USER_ID, GatewayFixtures.USER_EMAIL) }
    }

    @Test
    fun `signIn returns LOCKED error even when requestVerificationEmail fails`() {
        every { userDetailsService.findEdukateUserDetailsByUsername(GatewayFixtures.USER_NAME) } returns
            Mono.just(pendingUserDetails)
        every { passwordEncoder.matches(GatewayFixtures.RAW_PASSWORD, GatewayFixtures.ENCODED_PASSWORD) } returns true
        every { backendService.requestVerificationEmail(GatewayFixtures.USER_ID, GatewayFixtures.USER_EMAIL) } returns
            Mono.error(RuntimeException("email service unavailable"))

        StepVerifier.create(authService.signIn(GatewayFixtures.signInRequest()))
            .expectErrorSatisfies { error ->
                assertThat(error).isInstanceOf(ResponseStatusException::class.java)
                assertThat((error as ResponseStatusException).statusCode).isEqualTo(HttpStatus.LOCKED)
            }
            .verify()
    }

    // ── signUp ────────────────────────────────────────────────────────────────

    @Test
    fun `signUp completes when username is available and user is created`() {
        val request = GatewayFixtures.signUpRequest()
        every { userDetailsService.isNotUserPresent(request.username) } returns Mono.just(true)
        every { passwordEncoder.encode(request.password) } returns GatewayFixtures.ENCODED_PASSWORD
        every { userDetailsService.create(request.username, request.email, GatewayFixtures.ENCODED_PASSWORD) } returns
            Mono.just(activeUserDetails)
        every { backendService.requestVerificationEmail(GatewayFixtures.USER_ID, request.email) } returns Mono.empty()

        StepVerifier.create(authService.signUp(request)).verifyComplete()
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
        every { userDetailsService.create(any(), any(), any()) } returns Mono.just(activeUserDetails)
        every { backendService.requestVerificationEmail(any(), any()) } returns Mono.empty()

        StepVerifier.create(authService.signUp(request)).verifyComplete()

        verify(exactly = 1) { passwordEncoder.encode(request.password) }
    }

    // ── resendVerification ────────────────────────────────────────────────────

    @Test
    fun `resendVerification calls requestVerificationEmail when user is PENDING`() {
        val pendingCredentials = GatewayFixtures.userCredentials(status = UserStatus.PENDING)
        every { backendService.getUserByEmail(GatewayFixtures.USER_EMAIL) } returns Mono.just(pendingCredentials)
        every { backendService.requestVerificationEmail(GatewayFixtures.USER_ID, GatewayFixtures.USER_EMAIL) } returns
            Mono.empty()

        StepVerifier.create(authService.resendVerification(GatewayFixtures.USER_EMAIL)).verifyComplete()

        verify(exactly = 1) { backendService.requestVerificationEmail(GatewayFixtures.USER_ID, GatewayFixtures.USER_EMAIL) }
    }

    @Test
    fun `resendVerification does NOT call requestVerificationEmail when user is ACTIVE`() {
        val activeCredentials = GatewayFixtures.userCredentials(status = UserStatus.ACTIVE)
        every { backendService.getUserByEmail(GatewayFixtures.USER_EMAIL) } returns Mono.just(activeCredentials)

        StepVerifier.create(authService.resendVerification(GatewayFixtures.USER_EMAIL)).verifyComplete()

        verify(exactly = 0) { backendService.requestVerificationEmail(any(), any()) }
    }

    @Test
    fun `resendVerification completes empty when user is not found`() {
        every { backendService.getUserByEmail(GatewayFixtures.USER_EMAIL) } returns Mono.empty()

        StepVerifier.create(authService.resendVerification(GatewayFixtures.USER_EMAIL)).verifyComplete()

        verify(exactly = 0) { backendService.requestVerificationEmail(any(), any()) }
    }

    @Test
    fun `resendVerification completes empty when requestVerificationEmail fails`() {
        val pendingCredentials = GatewayFixtures.userCredentials(status = UserStatus.PENDING)
        every { backendService.getUserByEmail(GatewayFixtures.USER_EMAIL) } returns Mono.just(pendingCredentials)
        every { backendService.requestVerificationEmail(GatewayFixtures.USER_ID, GatewayFixtures.USER_EMAIL) } returns
            Mono.error(RuntimeException("smtp failure"))

        StepVerifier.create(authService.resendVerification(GatewayFixtures.USER_EMAIL)).verifyComplete()
    }
}
