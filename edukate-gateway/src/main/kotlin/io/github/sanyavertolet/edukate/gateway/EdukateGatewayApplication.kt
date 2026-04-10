package io.github.sanyavertolet.edukate.gateway

import io.github.sanyavertolet.edukate.gateway.configs.GatewayProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(GatewayProperties::class)
@SpringBootApplication(
    scanBasePackages =
        [
            "io.github.sanyavertolet.edukate.auth",
            "io.github.sanyavertolet.edukate.common.configs",
            "io.github.sanyavertolet.edukate.gateway",
        ]
)
class EdukateGatewayApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<EdukateGatewayApplication>(*args)
}
