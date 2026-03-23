package io.github.sanyavertolet.edukate.gateway.security

import io.github.sanyavertolet.edukate.common.utils.PublicEndpoints
import io.github.sanyavertolet.edukate.gateway.filters.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
@Profile("secure")
class WebSecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    @param:Value($$"${cors.allowed-origin-pattern}") private val corsAllowedOriginPattern: String,
) {
    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedHeaders = listOf("Content-Type", "api_key")
        configuration.allowedOriginPatterns = listOf(corsAllowedOriginPattern)
        configuration.maxAge = COOKIE_MAX_AGE
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    @Order(2)
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/internal/**")
                    .denyAll()
                    .pathMatchers(*PublicEndpoints.asArray())
                    .permitAll()
                    .pathMatchers("/api/**")
                    .authenticated()
            }
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .build()

    @Bean
    @Order(1)
    fun publicEndpointsSecurityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .securityMatcher(PublicEndpoints.asMatcher())
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .authorizeExchange { it.anyExchange().permitAll() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val delegatingPasswordEncoder =
            PasswordEncoderFactories.createDelegatingPasswordEncoder() as DelegatingPasswordEncoder
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(BCryptPasswordEncoder())
        return delegatingPasswordEncoder
    }

    companion object {
        private const val COOKIE_MAX_AGE = 3600L
    }
}
