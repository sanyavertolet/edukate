package io.github.sanyavertolet.edukate.common.security

import io.github.sanyavertolet.edukate.common.utils.PublicEndpoints
import io.github.sanyavertolet.edukate.common.utils.toEdukateUserDetails
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import reactor.kotlin.core.publisher.toMono

@Configuration
@Profile("secure")
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebSecurityConfig {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .authorizeExchange {
                it.matchers(PublicEndpoints.exchangeMatcher).permitAll().pathMatchers("/api/**").authenticated()
            }
            .addFilterAt(edukateUserPreAuthenticatedProcessingWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .csrf { it.disable() }
            .cors { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .build()

    @Bean
    fun edukateUserPreAuthenticatedProcessingWebFilter(): WebFilter {
        val authenticationManager = ReactiveAuthenticationManager { it.toMono() }
        val authenticationWebFilter = AuthenticationWebFilter(authenticationManager)

        authenticationWebFilter.setServerAuthenticationConverter { exchange: ServerWebExchange ->
            exchange.request.headers
                .toMono()
                .mapNotNull { it.toEdukateUserDetails() }
                .map { it.toPreAuthenticatedAuthenticationToken() }
        }
        return authenticationWebFilter
    }
}
