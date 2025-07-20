package io.github.sanyavertolet.edukate.gateway.filters;

import io.github.sanyavertolet.edukate.common.EdukateUserDetails;
import io.github.sanyavertolet.edukate.auth.services.AuthCookieService;
import io.github.sanyavertolet.edukate.auth.services.JwtTokenService;
import io.github.sanyavertolet.edukate.common.utils.HttpHeadersUtils;
import io.github.sanyavertolet.edukate.gateway.services.UserDetailsService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.function.Tuples;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {
    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;
    private final AuthCookieService authCookieService;

    private ServerHttpRequest getModifierRequest(ServerWebExchange exchange, EdukateUserDetails userDetails) {
        return exchange.getRequest().mutate().headers(headers ->
                HttpHeadersUtils.populateHeaders(headers, userDetails)
        ).build();
    }

    @NonNull
    private ServerWebExchange modifyRequestHeaders(
            @NonNull ServerWebExchange exchange,
            @NonNull EdukateUserDetails userDetails
    ) {
        ServerHttpRequest modifiedRequest = getModifierRequest(exchange, userDetails);
        return exchange.mutate().request(modifiedRequest).build();
    }

    private Context createAuthContext(@NonNull EdukateUserDetails userDetails) {
        return ReactiveSecurityContextHolder.withAuthentication(
                userDetails.toPreAuthenticatedAuthenticationToken()
        );
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return authCookieService.ejectToken(exchange)
                .map(jwtTokenService::getUserDetailsFromToken)
                .map(EdukateUserDetails::getId)
                .flatMap(userDetailsService::findById)
                .map(userDetails -> Tuples.of(
                        modifyRequestHeaders(exchange, userDetails),
                        createAuthContext(userDetails)
                ))
                .defaultIfEmpty(Tuples.of(exchange, Context.empty()))
                .flatMap(tuple ->
                        chain.filter(tuple.getT1()).contextWrite(tuple.getT2())
                );
    }
}
