package io.github.sanyavertolet.edukate.auth.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
public class AuthCookieService {
    private static final String AUTHORIZATION_COOKIE = "X-Auth";

    @Value("${jwt.expirationSeconds}")
    private long expirationTimeSeconds;

    private ResponseCookie createTokenCookie(String token) {
        return ResponseCookie.from(AUTHORIZATION_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(expirationTimeSeconds)
                .sameSite(Cookie.SameSite.STRICT.toString())
                .build();
    }

    private ResponseCookie createExpiredTokenCookie() {
        return ResponseCookie.from(AUTHORIZATION_COOKIE, "expired")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite(Cookie.SameSite.STRICT.toString())
                .build();
    }

    public Mono<String> ejectToken(ServerWebExchange exchange) {
        return Mono.fromCallable(exchange::getRequest)
                .map(ServerHttpRequest::getCookies)
                .mapNotNull(cookies -> cookies.getFirst(AUTHORIZATION_COOKIE))
                .map(HttpCookie::getValue);
    }

    public Mono<ResponseEntity<Void>> respondWithToken(String token) {
        return Mono.justOrEmpty(token)
                .map(this::createTokenCookie)
                .map(cookie ->
                        ResponseEntity.noContent().header("Set-Cookie", cookie.toString()).build()
                );
    }

    public Mono<ResponseEntity<Void>> respondWithExpiredToken() {
        return Mono.fromCallable(this::createExpiredTokenCookie).map(cookie ->
                ResponseEntity.noContent().header("Set-Cookie", cookie.toString()).build()
        );
    }
}
