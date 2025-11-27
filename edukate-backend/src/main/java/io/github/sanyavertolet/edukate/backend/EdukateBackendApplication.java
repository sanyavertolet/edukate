package io.github.sanyavertolet.edukate.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {
        "io.github.sanyavertolet.edukate.backend",
        "io.github.sanyavertolet.edukate.common",
        "io.github.sanyavertolet.edukate.storage",
})
@ConfigurationPropertiesScan(basePackages = "io.github.sanyavertolet.edukate.storage.configs")
public class EdukateBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(EdukateBackendApplication.class, args);
    }
}
