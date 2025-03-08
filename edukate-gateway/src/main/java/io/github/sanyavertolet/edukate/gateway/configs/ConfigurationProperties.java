package io.github.sanyavertolet.edukate.gateway.configs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationPropertiesScan
@org.springframework.boot.context.properties.ConfigurationProperties(prefix = "gateway")
public class ConfigurationProperties {
    private Backend backend;

    @Getter
    @RequiredArgsConstructor
    public static class Backend {
        private final String url;
    }
}
