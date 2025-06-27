package io.github.sanyavertolet.edukate.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "io.github.sanyavertolet.edukate.backend",
        "io.github.sanyavertolet.edukate.auth.configs",
        "io.github.sanyavertolet.edukate.common.services"
})
public class EdukateBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(EdukateBackendApplication.class, args);
    }
}
