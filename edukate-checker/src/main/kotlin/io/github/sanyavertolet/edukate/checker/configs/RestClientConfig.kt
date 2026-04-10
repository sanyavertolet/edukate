package io.github.sanyavertolet.edukate.checker.configs

import java.time.Duration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {
    /** Spring AI uses RestClient.Builder to build a model client but provides no way to configure timeouts. */
    @Bean
    fun restClientBuilder(
        @Value("\${spring.http.client.connect-timeout:1s}") connectTimeout: Duration,
        @Value("\${spring.http.client.read-timeout:10s}") readTimeout: Duration,
    ): RestClient.Builder {
        val settings =
            ClientHttpRequestFactorySettings.defaults().withConnectTimeout(connectTimeout).withReadTimeout(readTimeout)
        return RestClient.builder().requestFactory(ClientHttpRequestFactoryBuilder.reactor().build(settings))
    }
}
