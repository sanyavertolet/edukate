package io.github.sanyavertolet.edukate.notifier

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages =
        [
            "io.github.sanyavertolet.edukate.common.configs",
            "io.github.sanyavertolet.edukate.common.security",
            "io.github.sanyavertolet.edukate.notifier",
        ]
)
class EdukateNotifierApplication

fun main(args: Array<String>) {
    runApplication<EdukateNotifierApplication>(*args)
}
