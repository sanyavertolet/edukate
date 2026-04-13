package io.github.sanyavertolet.edukate.checker.configs

import java.time.Duration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.http.client.HttpClientSettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {
    /** Spring AI uses RestClient.Builder to build a model client but provides no way to configure timeouts. */
    @Bean
    fun restClientBuilder(
        @Value($$"${spring.http.clients.connect-timeout:5s}") connectTimeout: Duration,
        @Value($$"${spring.http.clients.read-timeout:2m}") readTimeout: Duration,
    ): RestClient.Builder {
        val settings = HttpClientSettings.defaults().withConnectTimeout(connectTimeout).withReadTimeout(readTimeout)
        return RestClient.builder().requestFactory(ClientHttpRequestFactoryBuilder.reactor().build(settings))
    }
}
