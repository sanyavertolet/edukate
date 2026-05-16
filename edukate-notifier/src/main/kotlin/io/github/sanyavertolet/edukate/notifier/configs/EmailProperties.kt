package io.github.sanyavertolet.edukate.notifier.configs

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "edukate.email")
data class EmailProperties(
    val from: String,
    val verificationTtlSeconds: Long = 86400,
    val passwordResetTtlSeconds: Long = 3600,
)
