package io.github.sanyavertolet.edukate.gateway.filters

import io.github.sanyavertolet.edukate.auth.services.AuthCookieService
import io.github.sanyavertolet.edukate.auth.services.JwtTokenService
import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import io.github.sanyavertolet.edukate.common.utils.populateHeaders
import io.github.sanyavertolet.edukate.gateway.services.UserDetailsService
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context

@Component
class JwtAuthenticationFilter(
    private val jwtTokenService: JwtTokenService,
    private val userDetailsService: UserDetailsService,
    private val authCookieService: AuthCookieService,
) : WebFilter {
    private fun getModifiedRequest(exchange: ServerWebExchange, userDetails: EdukateUserDetails): ServerHttpRequest =
        exchange.request.mutate().headers { populateHeaders(it, userDetails) }.build()

    private fun modifyRequestHeaders(exchange: ServerWebExchange, userDetails: EdukateUserDetails): ServerWebExchange =
        exchange.mutate().request(getModifiedRequest(exchange, userDetails)).build()

    private fun createAuthContext(userDetails: EdukateUserDetails): Context =
        ReactiveSecurityContextHolder.withAuthentication(userDetails.toPreAuthenticatedAuthenticationToken())

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
        authCookieService
            .ejectToken(exchange)
            .mapNotNull { jwtTokenService.getUserDetailsFromToken(it) }
            .map { it.id }
            .flatMap { userDetailsService.findById(it) }
            .map { modifyRequestHeaders(exchange, it) to createAuthContext(it) }
            .defaultIfEmpty(exchange to Context.empty())
            .flatMap { (exchange, context) -> chain.filter(exchange).contextWrite(context) }
}
