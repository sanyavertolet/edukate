package io.github.sanyavertolet.edukate.common.utils

import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher

object PublicEndpoints {
    private val endpoints =
        listOf("/actuator/**", "/internal/**", "/api/v1/problems/**", "/api/v1/auth/*", "/swagger/**", "/swagger-ui/**")

    @JvmStatic fun asArray(): Array<String> = endpoints.toTypedArray<String>()

    @JvmStatic
    fun asMatcher(): ServerWebExchangeMatcher =
        endpoints
            .map { PathPatternParserServerWebExchangeMatcher(it) }
            .map { it as ServerWebExchangeMatcher }
            .let { OrServerWebExchangeMatcher(it) }
}
