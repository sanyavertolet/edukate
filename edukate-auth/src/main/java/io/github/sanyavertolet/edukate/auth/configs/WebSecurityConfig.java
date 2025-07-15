package io.github.sanyavertolet.edukate.auth.configs;

import io.github.sanyavertolet.edukate.auth.EdukateUserDetails;
import io.github.sanyavertolet.edukate.auth.utils.HttpHeadersUtils;
import io.github.sanyavertolet.edukate.auth.utils.PublicEndpoints;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
@Profile("secure")
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PublicEndpoints.asArray()).permitAll()
                        .pathMatchers("/api/**").authenticated())
                .addFilterAt(edukateUserPreAuthenticatedProcessingWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(exceptionHandlingSpec ->
                        exceptionHandlingSpec.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .build();
    }

    @Bean
    public WebFilter edukateUserPreAuthenticatedProcessingWebFilter() {
        ReactiveAuthenticationManager authenticationManager = Mono::just;
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);

        authenticationWebFilter.setServerAuthenticationConverter(exchange ->
                Mono.just(exchange.getRequest().getHeaders())
                        .mapNotNull(HttpHeadersUtils::toEdukateUserDetails)
                        .map(EdukateUserDetails::toPreAuthenticatedAuthenticationToken));
        return authenticationWebFilter;
    }
}
