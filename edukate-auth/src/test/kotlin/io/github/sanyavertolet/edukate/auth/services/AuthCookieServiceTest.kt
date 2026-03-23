package io.github.sanyavertolet.edukate.auth.services

import io.github.sanyavertolet.edukate.auth.AuthFixtures
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.env.Environment
import org.springframework.http.HttpCookie
import org.springframework.http.HttpStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.server.ServerWebExchange
import reactor.test.StepVerifier

class AuthCookieServiceTest {
    private fun buildService(isDev: Boolean = false): AuthCookieService {
        val env = mockk<Environment>()
        every { env.matchesProfiles("dev") } returns isDev
        return AuthCookieService(expirationTimeSeconds = 3600L, hostname = AuthFixtures.HOSTNAME, environment = env)
    }

    private fun exchangeWithCookie(name: String, value: String): ServerWebExchange {
        val request = mockk<org.springframework.http.server.reactive.ServerHttpRequest>()
        val cookies = LinkedMultiValueMap<String, HttpCookie>()
        cookies.add(name, HttpCookie(name, value))
        every { request.cookies } returns cookies
        val exchange = mockk<ServerWebExchange>()
        every { exchange.request } returns request
        return exchange
    }

    private fun exchangeWithoutCookie(): ServerWebExchange {
        val request = mockk<org.springframework.http.server.reactive.ServerHttpRequest>()
        every { request.cookies } returns LinkedMultiValueMap()
        val exchange = mockk<ServerWebExchange>()
        every { exchange.request } returns request
        return exchange
    }

    // --- ejectToken ---

    @Test
    fun `ejectToken returns token from X-Auth cookie`() {
        val service = buildService()
        val exchange = exchangeWithCookie("X-Auth", "my-token")

        StepVerifier.create(service.ejectToken(exchange)).expectNext("my-token").verifyComplete()
    }

    @Test
    fun `ejectToken returns empty Mono when cookie absent`() {
        val service = buildService()
        val exchange = exchangeWithoutCookie()

        StepVerifier.create(service.ejectToken(exchange)).verifyComplete()
    }

    @Test
    fun `ejectToken returns empty Mono when X-Auth not in cookies`() {
        val service = buildService()
        val exchange = exchangeWithCookie("Other-Cookie", "some-value")

        StepVerifier.create(service.ejectToken(exchange)).verifyComplete()
    }

    // --- respondWithToken ---

    @Test
    fun `respondWithToken returns 204`() {
        val service = buildService()

        StepVerifier.create(service.respondWithToken("tok"))
            .assertNext { assertThat(it.statusCode).isEqualTo(HttpStatus.NO_CONTENT) }
            .verifyComplete()
    }

    @Test
    fun `respondWithToken sets Set-Cookie header containing token`() {
        val service = buildService()

        StepVerifier.create(service.respondWithToken("my-token"))
            .assertNext { assertThat(it.headers.getFirst("Set-Cookie")).contains("X-Auth=my-token") }
            .verifyComplete()
    }

    @Test
    fun `respondWithToken cookie is HttpOnly`() {
        val service = buildService()

        StepVerifier.create(service.respondWithToken("tok"))
            .assertNext { assertThat(it.headers.getFirst("Set-Cookie")).contains("HttpOnly") }
            .verifyComplete()
    }

    @Test
    fun `respondWithToken cookie path is slash`() {
        val service = buildService()

        StepVerifier.create(service.respondWithToken("tok"))
            .assertNext { assertThat(it.headers.getFirst("Set-Cookie")).contains("Path=/") }
            .verifyComplete()
    }

    @Test
    fun `respondWithToken cookie domain matches hostname`() {
        val service = buildService()

        StepVerifier.create(service.respondWithToken("tok"))
            .assertNext { assertThat(it.headers.getFirst("Set-Cookie")).contains("Domain=localhost") }
            .verifyComplete()
    }

    @Test
    fun `respondWithToken cookie SameSite is Strict`() {
        val service = buildService()

        StepVerifier.create(service.respondWithToken("tok"))
            .assertNext { assertThat(it.headers.getFirst("Set-Cookie")).containsIgnoringCase("SameSite=Strict") }
            .verifyComplete()
    }

    @Test
    fun `respondWithToken cookie maxAge equals expirationSeconds`() {
        val service = buildService()

        StepVerifier.create(service.respondWithToken("tok"))
            .assertNext { assertThat(it.headers.getFirst("Set-Cookie")).contains("Max-Age=3600") }
            .verifyComplete()
    }

    @Test
    fun `respondWithToken cookie is Secure in non-dev profile`() {
        val service = buildService(isDev = false)

        StepVerifier.create(service.respondWithToken("tok"))
            .assertNext { assertThat(it.headers.getFirst("Set-Cookie")).contains("Secure") }
            .verifyComplete()
    }

    @Test
    fun `respondWithToken cookie is not Secure in dev profile`() {
        val service = buildService(isDev = true)

        StepVerifier.create(service.respondWithToken("tok"))
            .assertNext { assertThat(it.headers.getFirst("Set-Cookie")).doesNotContain("Secure") }
            .verifyComplete()
    }

    // --- respondWithExpiredToken ---

    @Test
    fun `respondWithExpiredToken returns 204`() {
        val service = buildService()

        StepVerifier.create(service.respondWithExpiredToken())
            .assertNext { assertThat(it.statusCode).isEqualTo(HttpStatus.NO_CONTENT) }
            .verifyComplete()
    }

    @Test
    fun `respondWithExpiredToken sets maxAge to 0`() {
        val service = buildService()

        StepVerifier.create(service.respondWithExpiredToken())
            .assertNext { assertThat(it.headers.getFirst("Set-Cookie")).contains("Max-Age=0") }
            .verifyComplete()
    }

    @Test
    fun `respondWithExpiredToken sets cookie value to expired`() {
        val service = buildService()

        StepVerifier.create(service.respondWithExpiredToken())
            .assertNext { assertThat(it.headers.getFirst("Set-Cookie")).contains("X-Auth=expired") }
            .verifyComplete()
    }

    @Test
    fun `respondWithExpiredToken cookie is HttpOnly`() {
        val service = buildService()

        StepVerifier.create(service.respondWithExpiredToken())
            .assertNext { assertThat(it.headers.getFirst("Set-Cookie")).contains("HttpOnly") }
            .verifyComplete()
    }
}
