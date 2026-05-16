package io.github.sanyavertolet.edukate.notifier

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages =
        [
            "io.github.sanyavertolet.edukate.common.configs",
            "io.github.sanyavertolet.edukate.common.security",
            "io.github.sanyavertolet.edukate.notifier",
        ]
)
@ConfigurationPropertiesScan(basePackages = ["io.github.sanyavertolet.edukate.notifier.configs"])
class EdukateNotifierApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<EdukateNotifierApplication>(*args)
}
