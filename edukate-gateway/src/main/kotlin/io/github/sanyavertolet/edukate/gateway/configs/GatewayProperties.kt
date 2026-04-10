package io.github.sanyavertolet.edukate.gateway.configs

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gateway")
data class GatewayProperties(val backend: Backend) {
    data class Backend(val url: String)
}
