package io.github.sanyavertolet.edukate.storage.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "s3")
public class S3Properties {
    private String endpoint;

    private String region;

    private String accessKey;

    private String secretKey;

    private String bucket;
}
