package io.github.sanyavertolet.edukate.gateway.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Getter
@Setter
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {
    private Backend backend;

    public record Backend(String url) { }
}
