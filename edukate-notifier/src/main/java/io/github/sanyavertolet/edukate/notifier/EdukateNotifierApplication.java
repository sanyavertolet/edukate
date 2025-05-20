package io.github.sanyavertolet.edukate.notifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "io.github.sanyavertolet.edukate.notifier",
        "io.github.sanyavertolet.edukate.auth.configs",
})
public class EdukateNotifierApplication {
    public static void main(String[] args) {
        SpringApplication.run(EdukateNotifierApplication.class, args);
    }
}
