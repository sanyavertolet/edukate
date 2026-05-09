package io.github.sanyavertolet.edukate.backend.configs

import io.github.sanyavertolet.edukate.common.ContentLanguage
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context

@Component
class LanguageWebFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val language =
            exchange.request.headers.acceptLanguage.firstOrNull()?.range?.lowercase()?.let { range ->
                ContentLanguage.entries.firstOrNull { range.startsWith(it.name.lowercase()) }
            } ?: ContentLanguage.RU
        return chain.filter(exchange).contextWrite(Context.of(LANGUAGE_KEY, language))
    }

    companion object {
        const val LANGUAGE_KEY = "edukate.language"

        fun fromContext(ctx: reactor.util.context.ContextView): ContentLanguage =
            ctx.getOrDefault(LANGUAGE_KEY, ContentLanguage.RU)!!
    }
}
