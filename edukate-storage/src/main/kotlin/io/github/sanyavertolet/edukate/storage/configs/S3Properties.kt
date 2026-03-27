package io.github.sanyavertolet.edukate.storage.configs

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "s3")
open class S3Properties {
    lateinit var endpoint: String
    lateinit var region: String
    lateinit var accessKey: String
    lateinit var secretKey: String
    lateinit var bucket: String
    lateinit var signatureDuration: Duration
}
