package io.github.sanyavertolet.edukate.checker.storage

import io.github.sanyavertolet.edukate.storage.AbstractReadOnlyStorage
import io.github.sanyavertolet.edukate.storage.configs.S3Properties
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Component
class RawKeyReadOnlyStorage(s3AsyncClient: S3AsyncClient, s3Presigner: S3Presigner, s3Properties: S3Properties) :
    AbstractReadOnlyStorage<String, MediaType>(s3AsyncClient, s3Presigner, s3Properties) {

    override fun buildKey(key: String): String = key

    override fun buildMetadata(headObjectResponse: HeadObjectResponse): MediaType {
        log.debug("Identified content type of: {}", headObjectResponse.contentType())
        return MediaType.parseMediaType(headObjectResponse.contentType())
    }

    companion object {
        private val log = LoggerFactory.getLogger(RawKeyReadOnlyStorage::class.java)
    }
}
