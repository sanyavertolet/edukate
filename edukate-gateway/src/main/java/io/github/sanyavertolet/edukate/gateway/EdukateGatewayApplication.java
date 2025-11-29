package io.github.sanyavertolet.edukate.gateway;

import io.github.sanyavertolet.edukate.gateway.configs.GatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(GatewayProperties.class)
@SpringBootApplication(scanBasePackages = {
        "io.github.sanyavertolet.edukate.auth",
        "io.github.sanyavertolet.edukate.common.configs",
        "io.github.sanyavertolet.edukate.gateway",
})
public class EdukateGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(EdukateGatewayApplication.class, args);
    }
}
