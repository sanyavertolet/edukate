package io.github.sanyavertolet.edukate.checker.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient backendClient(
            WebClient.Builder builder,
            @Value("${checker.backend.url:http://localhost:8080}") String baseUrl,
            @Value("${checker.backend.read-timeout-ms:30000}") long readTimeoutMs,
            @Value("${checker.backend.connect-timeout-ms:5000}") int connectTimeoutMs
    ) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(readTimeoutMs))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs);

        return builder.baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

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
