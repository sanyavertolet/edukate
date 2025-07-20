package io.github.sanyavertolet.edukate.notifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "io.github.sanyavertolet.edukate.common.configs",
        "io.github.sanyavertolet.edukate.common.security",
        "io.github.sanyavertolet.edukate.notifier"
})
public class EdukateNotifierApplication {
    public static void main(String[] args) {
        SpringApplication.run(EdukateNotifierApplication.class, args);
    }
}
