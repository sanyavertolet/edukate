package io.github.sanyavertolet.edukate.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages =
        [
            "io.github.sanyavertolet.edukate.backend",
            "io.github.sanyavertolet.edukate.common",
            "io.github.sanyavertolet.edukate.storage",
        ]
)
@ConfigurationPropertiesScan(basePackages = ["io.github.sanyavertolet.edukate.storage.configs"])
class EdukateBackendApplication

fun main(args: Array<String>) {
    runApplication<EdukateBackendApplication>(*args)
}
