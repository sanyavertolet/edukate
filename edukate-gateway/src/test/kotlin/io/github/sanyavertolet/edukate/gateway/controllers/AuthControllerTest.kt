@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.gateway.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.auth.services.AuthCookieService
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.github.sanyavertolet.edukate.gateway.filters.JwtAuthenticationFilter
import io.github.sanyavertolet.edukate.gateway.services.AuthService
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

// "test" profile prevents the default "prod,secure" profiles (set in application.yml) from
// activating,
// which allows NoopWebSecurityConfig (@Profile("!secure")) to be imported and loaded correctly.
@WebFluxTest(AuthController::class)
@Import(NoopWebSecurityConfig::class)
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var authService: AuthService

    @MockkBean private lateinit var authCookieService: AuthCookieService

    // JwtAuthenticationFilter is a @Component WebFilter that gets loaded by @WebFluxTest;
    // mock it to pass through so controller tests are not affected by filter logic.
    @MockkBean private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @BeforeEach
    fun setup() {
        every { jwtAuthenticationFilter.filter(any(), any()) } answers { secondArg<WebFilterChain>().filter(firstArg()) }
    }

    // ── POST /api/v1/auth/sign-in ──────────────────────────────────────────────

    @Test
    fun `signIn returns 204 and Set-Cookie header when credentials are valid`() {
        every { authService.signIn(any()) } returns Mono.just("jwt-token")
        every { authCookieService.respondWithToken("jwt-token") } returns
            Mono.just(ResponseEntity.noContent().header("Set-Cookie", "X-Auth=jwt-token; Path=/").build())

        webTestClient
            .post()
            .uri("/api/v1/auth/sign-in")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"username":"testuser","password":"raw-password"}""")
            .exchange()
            .expectStatus()
            .isNoContent
            .expectHeader()
            .exists("Set-Cookie")
    }

    @Test
    fun `signIn returns 403 when authService returns empty Mono`() {
        every { authService.signIn(any()) } returns Mono.empty()

        webTestClient
            .post()
            .uri("/api/v1/auth/sign-in")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"username":"testuser","password":"raw-password"}""")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `signIn returns 400 when username is blank`() {
        webTestClient
            .post()
            .uri("/api/v1/auth/sign-in")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"username":"","password":"secret"}""")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `signIn returns 400 when password is blank`() {
        webTestClient
            .post()
            .uri("/api/v1/auth/sign-in")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"username":"alice","password":""}""")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `signIn returns 400 when body is missing`() {
        webTestClient.post().uri("/api/v1/auth/sign-in").exchange().expectStatus().isBadRequest
    }

    // ── POST /api/v1/auth/sign-up ──────────────────────────────────────────────

    @Test
    fun `signUp returns 204 and Set-Cookie header on successful registration`() {
        every { authService.signUp(any()) } returns Mono.just("jwt-token")
        every { authCookieService.respondWithToken("jwt-token") } returns
            Mono.just(ResponseEntity.noContent().header("Set-Cookie", "X-Auth=jwt-token; Path=/").build())

        webTestClient
            .post()
            .uri("/api/v1/auth/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"username":"alice","password":"secret","email":"alice@example.com"}""")
            .exchange()
            .expectStatus()
            .isNoContent
            .expectHeader()
            .exists("Set-Cookie")
    }

    @Test
    fun `signUp returns 403 when authService returns empty Mono`() {
        every { authService.signUp(any()) } returns Mono.empty()

        webTestClient
            .post()
            .uri("/api/v1/auth/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"username":"alice","password":"secret","email":"alice@example.com"}""")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `signUp returns 409 when username already exists`() {
        every { authService.signUp(any()) } returns Mono.error(ResponseStatusException(HttpStatus.CONFLICT))

        webTestClient
            .post()
            .uri("/api/v1/auth/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"username":"alice","password":"secret","email":"alice@example.com"}""")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `signUp returns 400 when username is blank`() {
        webTestClient
            .post()
            .uri("/api/v1/auth/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"username":"","password":"p","email":"a@b.com"}""")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `signUp returns 400 when email is invalid`() {
        webTestClient
            .post()
            .uri("/api/v1/auth/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"username":"alice","password":"p","email":"not-an-email"}""")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `signUp returns 400 when body is missing`() {
        webTestClient.post().uri("/api/v1/auth/sign-up").exchange().expectStatus().isBadRequest
    }

    // ── POST /api/v1/auth/sign-out ─────────────────────────────────────────────

    @Test
    fun `signOut returns 204 and clears cookie with Max-Age=0`() {
        every { authCookieService.respondWithExpiredToken() } returns
            Mono.just(ResponseEntity.noContent().header("Set-Cookie", "X-Auth=expired; Max-Age=0; Path=/").build())

        webTestClient
            .post()
            .uri("/api/v1/auth/sign-out")
            .exchange()
            .expectStatus()
            .isNoContent
            .expectHeader()
            .valueMatches("Set-Cookie", ".*Max-Age=0.*")
    }

    @Test
    fun `signOut returns 204 when no cookie is present in request`() {
        every { authCookieService.respondWithExpiredToken() } returns
            Mono.just(ResponseEntity.noContent().header("Set-Cookie", "X-Auth=expired; Max-Age=0; Path=/").build())

        webTestClient.post().uri("/api/v1/auth/sign-out").exchange().expectStatus().isNoContent
    }
}
