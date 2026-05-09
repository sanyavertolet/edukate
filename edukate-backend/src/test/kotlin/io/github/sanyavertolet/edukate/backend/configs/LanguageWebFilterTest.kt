package io.github.sanyavertolet.edukate.backend.configs

import io.github.sanyavertolet.edukate.common.ContentLanguage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class LanguageWebFilterTest {
    private val filter = LanguageWebFilter()

    @Test
    fun `defaults to RU when no Accept-Language header`() {
        val request = MockServerHttpRequest.get("/api/v1/problems").build()
        val exchange = MockServerWebExchange.from(request)

        var capturedLanguage: ContentLanguage? = null
        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                capturedLanguage = LanguageWebFilter.fromContext(ctx)
                Mono.empty()
            }
        }

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()
        assertThat(capturedLanguage).isEqualTo(ContentLanguage.RU)
    }

    @Test
    fun `parses en Accept-Language header`() {
        val request = MockServerHttpRequest.get("/api/v1/problems").header(HttpHeaders.ACCEPT_LANGUAGE, "en").build()
        val exchange = MockServerWebExchange.from(request)

        var capturedLanguage: ContentLanguage? = null
        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                capturedLanguage = LanguageWebFilter.fromContext(ctx)
                Mono.empty()
            }
        }

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()
        assertThat(capturedLanguage).isEqualTo(ContentLanguage.EN)
    }

    @Test
    fun `parses en-US Accept-Language header as EN`() {
        val request = MockServerHttpRequest.get("/api/v1/problems").header(HttpHeaders.ACCEPT_LANGUAGE, "en-US").build()
        val exchange = MockServerWebExchange.from(request)

        var capturedLanguage: ContentLanguage? = null
        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                capturedLanguage = LanguageWebFilter.fromContext(ctx)
                Mono.empty()
            }
        }

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()
        assertThat(capturedLanguage).isEqualTo(ContentLanguage.EN)
    }

    @Test
    fun `parses ru Accept-Language header`() {
        val request = MockServerHttpRequest.get("/api/v1/problems").header(HttpHeaders.ACCEPT_LANGUAGE, "ru").build()
        val exchange = MockServerWebExchange.from(request)

        var capturedLanguage: ContentLanguage? = null
        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                capturedLanguage = LanguageWebFilter.fromContext(ctx)
                Mono.empty()
            }
        }

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()
        assertThat(capturedLanguage).isEqualTo(ContentLanguage.RU)
    }

    @Test
    fun `unknown language defaults to RU`() {
        val request = MockServerHttpRequest.get("/api/v1/problems").header(HttpHeaders.ACCEPT_LANGUAGE, "fr").build()
        val exchange = MockServerWebExchange.from(request)

        var capturedLanguage: ContentLanguage? = null
        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                capturedLanguage = LanguageWebFilter.fromContext(ctx)
                Mono.empty()
            }
        }

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()
        assertThat(capturedLanguage).isEqualTo(ContentLanguage.RU)
    }
}
