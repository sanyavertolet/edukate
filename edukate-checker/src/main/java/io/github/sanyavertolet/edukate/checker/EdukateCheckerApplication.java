package io.github.sanyavertolet.edukate.checker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "io.github.sanyavertolet.edukate.checker",
        "io.github.sanyavertolet.edukate.common",
})
public class EdukateCheckerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EdukateCheckerApplication.class, args);
    }
}
