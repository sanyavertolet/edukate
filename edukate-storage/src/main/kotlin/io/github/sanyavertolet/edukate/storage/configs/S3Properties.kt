package io.github.sanyavertolet.edukate.storage.configs

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "s3")
data class S3Properties(
    val endpoint: String,
    val region: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
    val signatureDuration: Duration,
    // When set, presigned URLs use this host instead of `endpoint`.
    // Useful when the backend reaches MinIO on localhost but clients need a reachable address.
    val publicEndpoint: String? = null,
)
