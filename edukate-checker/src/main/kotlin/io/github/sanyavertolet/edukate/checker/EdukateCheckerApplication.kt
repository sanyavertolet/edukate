package io.github.sanyavertolet.edukate.checker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages =
        [
            "io.github.sanyavertolet.edukate.checker",
            "io.github.sanyavertolet.edukate.common",
            "io.github.sanyavertolet.edukate.storage",
        ]
)
@ConfigurationPropertiesScan(basePackages = ["io.github.sanyavertolet.edukate.storage.configs"])
class EdukateCheckerApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<EdukateCheckerApplication>(*args)
}
