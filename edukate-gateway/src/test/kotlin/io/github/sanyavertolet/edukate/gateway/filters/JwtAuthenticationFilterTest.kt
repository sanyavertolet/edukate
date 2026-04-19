package io.github.sanyavertolet.edukate.gateway.filters

import io.github.sanyavertolet.edukate.auth.services.AuthCookieService
import io.github.sanyavertolet.edukate.auth.services.JwtTokenService
import io.github.sanyavertolet.edukate.common.utils.AuthHeaders
import io.github.sanyavertolet.edukate.gateway.GatewayFixtures
import io.github.sanyavertolet.edukate.gateway.services.UserDetailsService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class JwtAuthenticationFilterTest {
    private val jwtTokenService: JwtTokenService = mockk()
    private val userDetailsService: UserDetailsService = mockk()
    private val authCookieService: AuthCookieService = mockk()
    private val filter = JwtAuthenticationFilter(jwtTokenService, userDetailsService, authCookieService)

    private val userDetails = GatewayFixtures.edukateUserDetails()

    @Test
    fun `filter adds user headers and security context when valid JWT present in cookie`() {
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/test").build())
        val chain: WebFilterChain = mockk()
        val capturedExchange = slot<ServerWebExchange>()

        every { authCookieService.ejectToken(any()) } returns Mono.just("jwt-token")
        every { jwtTokenService.getUserDetailsFromToken("jwt-token") } returns userDetails
        every { userDetailsService.findById(GatewayFixtures.USER_ID) } returns Mono.just(userDetails)
        every { chain.filter(capture(capturedExchange)) } returns Mono.empty()

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()

        assertThat(capturedExchange.captured.request.headers[AuthHeaders.AUTHORIZATION_ID.headerName])
            .contains(GatewayFixtures.USER_ID.toString())
        assertThat(capturedExchange.captured.request.headers[AuthHeaders.AUTHORIZATION_NAME.headerName])
            .contains(GatewayFixtures.USER_NAME)
    }

    @Test
    fun `filter calls chain without modification when X-Auth cookie is absent`() {
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/test").build())
        val chain: WebFilterChain = mockk()
        val capturedExchange = slot<ServerWebExchange>()

        every { authCookieService.ejectToken(any()) } returns Mono.empty()
        every { chain.filter(capture(capturedExchange)) } returns Mono.empty()

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()

        assertThat(capturedExchange.captured.request.headers[AuthHeaders.AUTHORIZATION_ID.headerName]).isNull()
    }

    @Test
    fun `filter calls chain without modification when JWT is invalid`() {
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/test").build())
        val chain: WebFilterChain = mockk()
        val capturedExchange = slot<ServerWebExchange>()

        every { authCookieService.ejectToken(any()) } returns Mono.just("invalid-jwt")
        every { jwtTokenService.getUserDetailsFromToken("invalid-jwt") } returns null
        every { chain.filter(capture(capturedExchange)) } returns Mono.empty()

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()

        assertThat(capturedExchange.captured.request.headers[AuthHeaders.AUTHORIZATION_ID.headerName]).isNull()
    }

    @Test
    fun `filter calls chain without modification when user not found in backend`() {
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/test").build())
        val chain: WebFilterChain = mockk()
        val capturedExchange = slot<ServerWebExchange>()

        every { authCookieService.ejectToken(any()) } returns Mono.just("jwt-token")
        every { jwtTokenService.getUserDetailsFromToken("jwt-token") } returns userDetails
        every { userDetailsService.findById(GatewayFixtures.USER_ID) } returns Mono.empty()
        every { chain.filter(capture(capturedExchange)) } returns Mono.empty()

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()

        assertThat(capturedExchange.captured.request.headers[AuthHeaders.AUTHORIZATION_ID.headerName]).isNull()
    }
}
