package io.github.sanyavertolet.edukate.common.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfiguration {

    @Bean
    @ConditionalOnProperty(name = "gateway.url")
    public OpenAPI openAPI(@Value("${gateway.url}") String gatewayUrl) {
        return new OpenAPI().servers(List.of(
                new Server().url(gatewayUrl)
        ));
    }
}
