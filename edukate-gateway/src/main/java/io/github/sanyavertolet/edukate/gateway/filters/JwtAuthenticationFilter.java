package io.github.sanyavertolet.edukate.gateway.filters;

import io.github.sanyavertolet.edukate.auth.services.JwtTokenService;
import io.github.sanyavertolet.edukate.auth.utils.AuthUtils;
import io.github.sanyavertolet.edukate.gateway.services.UserDetailsService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
@Component
@Import(JwtTokenService.class)
public class JwtAuthenticationFilter implements WebFilter {
    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        HttpHeaders httpHeaders = exchange.getRequest().getHeaders();
        String token = AuthUtils.getTokenFromHeaders(httpHeaders);
        if (token == null) {
            return chain.filter(exchange);
        }

        return Mono.just(token)
                .map(jwtTokenService::getUserDetailsFromToken)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired token")))
                .flatMap(userDetailsService::checkUserDetails)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Token seems to be rotten as it differs from database")))
                .flatMap(userDetails -> {
                    Authentication auth = userDetails.toPreAuthenticatedAuthenticationToken();
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate().headers(headers -> {
                        headers.remove(HttpHeaders.AUTHORIZATION);
                        userDetails.populateHeaders(headers);
                    }).build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build())
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                });
    }
}
