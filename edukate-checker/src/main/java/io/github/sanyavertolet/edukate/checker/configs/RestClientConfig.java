package io.github.sanyavertolet.edukate.checker.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    /**
     *  Spring AI uses RestClient.Builder to build a model client but provides no way to configure timeouts.
     */
    @Bean
    public RestClient.Builder restClientBuilder(
            @Value("${spring.http.client.connect-timeout:1s}") Duration connectTimeout,
            @Value("${spring.http.client.read-timeout:10s}") Duration readTimeout
    ) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(connectTimeout)
                .withReadTimeout(readTimeout);
        return RestClient.builder().requestFactory(ClientHttpRequestFactoryBuilder.reactor().build(settings));
    }
}
