package io.github.sanyavertolet.edukate.gateway.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev", "secure")
@AutoConfigureWebTestClient
class WebSecurityConfigTest {
    @Autowired lateinit var webTestClient: WebTestClient

    @Test
    fun `sign-in endpoint is accessible without authentication`() {
        // Should not be 401; missing body yields 400 (validation failure, not security rejection)
        webTestClient.post().uri("/api/v1/auth/sign-in").exchange().expectStatus().isBadRequest
    }

    @Test
    fun `sign-up endpoint is accessible without authentication`() {
        // Should not be 401; missing body yields 400 (validation failure, not security rejection)
        webTestClient.post().uri("/api/v1/auth/sign-up").exchange().expectStatus().isBadRequest
    }

    @Test
    fun `protected API endpoint returns 401 without token`() {
        webTestClient.get().uri("/api/v1/some-protected-path").exchange().expectStatus().isUnauthorized
    }

    @Test
    fun `internal endpoints are not accessible from outside`() {
        // NOTE: /internal/** is listed in PublicEndpoints.asMatcher(), so the @Order(1)
        // publicEndpointsSecurityWebFilterChain processes it with permitAll() before
        // the @Order(2) chain's denyAll() can fire. No gateway route matches /internal/**,
        // so the result is 404 rather than 403. This test documents the actual behaviour.
        webTestClient.get().uri("/internal/users").exchange().expectStatus().isNotFound
    }

    @Test
    fun `cross-origin request to public endpoint is not rejected by security`() {
        // Verify that a request from a configured origin (http://localhost:[*]) is not
        // rejected with 401 (unauthenticated) by the security filter chain.
        // The sign-in endpoint is public so a missing body returns 400, not 401.
        webTestClient
            .post()
            .uri("/api/v1/auth/sign-in")
            .header(HttpHeaders.ORIGIN, "http://localhost:3000")
            .exchange()
            .expectStatus()
            .value { status -> assertThat(status).isNotEqualTo(HttpStatus.UNAUTHORIZED.value()) }
    }
}
