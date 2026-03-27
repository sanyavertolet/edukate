package io.github.sanyavertolet.edukate.storage.configs

import java.net.URI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Configuration
class S3Config {
    @Bean
    fun awsCredentialsProvider(s3Properties: S3Properties): AwsCredentialsProvider =
        StaticCredentialsProvider.create(AwsBasicCredentials.create(s3Properties.accessKey, s3Properties.secretKey))

    @Bean fun serviceConfiguration(): S3Configuration = S3Configuration.builder().pathStyleAccessEnabled(true).build()

    @Bean
    fun s3AsyncClient(
        credentialsProvider: AwsCredentialsProvider,
        s3Configuration: S3Configuration,
        s3Properties: S3Properties,
    ): S3AsyncClient =
        S3AsyncClient.builder()
            .region(Region.of(s3Properties.region))
            .endpointOverride(URI.create(s3Properties.endpoint))
            .credentialsProvider(credentialsProvider)
            .serviceConfiguration(s3Configuration)
            .build()

    @Bean
    fun s3Presigner(
        credentialsProvider: AwsCredentialsProvider,
        s3Configuration: S3Configuration,
        s3Properties: S3Properties,
    ): S3Presigner =
        S3Presigner.builder()
            .region(Region.of(s3Properties.region))
            .endpointOverride(URI.create(s3Properties.endpoint))
            .credentialsProvider(credentialsProvider)
            .serviceConfiguration(s3Configuration)
            .build()
}
