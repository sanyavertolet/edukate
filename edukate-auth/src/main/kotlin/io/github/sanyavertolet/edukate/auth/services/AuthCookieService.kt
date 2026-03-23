package io.github.sanyavertolet.edukate.auth.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.server.Cookie
import org.springframework.core.env.Environment
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Service
class AuthCookieService(
    @param:Value($$"${auth.jwt.expirationSeconds}") private val expirationTimeSeconds: Long,
    @param:Value($$"${auth.hostname}") private val hostname: String,
    environment: Environment,
) {
    private val isSecure = !environment.matchesProfiles("dev")

    private fun createTokenCookie(token: String): ResponseCookie =
        ResponseCookie.from(AUTHORIZATION_COOKIE, token)
            .httpOnly(true)
            .secure(isSecure)
            .domain(hostname)
            .path("/")
            .maxAge(expirationTimeSeconds)
            .sameSite(Cookie.SameSite.STRICT.toString())
            .build()

    private fun createExpiredTokenCookie(): ResponseCookie =
        ResponseCookie.from(AUTHORIZATION_COOKIE, "expired")
            .httpOnly(true)
            .secure(isSecure)
            .domain(hostname)
            .path("/")
            .maxAge(0)
            .sameSite(Cookie.SameSite.STRICT.toString())
            .build()

    fun ejectToken(exchange: ServerWebExchange): Mono<String> =
        Mono.fromCallable { exchange.request.cookies.getFirst(AUTHORIZATION_COOKIE)?.value }.mapNotNull { it }

    fun respondWithToken(token: String): Mono<ResponseEntity<Void>> =
        Mono.justOrEmpty(token)
            .map { createTokenCookie(it) }
            .map { ResponseEntity.noContent().header("Set-Cookie", it.toString()).build() }

    fun respondWithExpiredToken(): Mono<ResponseEntity<Void>> =
        Mono.fromCallable { createExpiredTokenCookie() }
            .map { ResponseEntity.noContent().header("Set-Cookie", it.toString()).build() }

    companion object {
        private const val AUTHORIZATION_COOKIE = "X-Auth"
    }
}
