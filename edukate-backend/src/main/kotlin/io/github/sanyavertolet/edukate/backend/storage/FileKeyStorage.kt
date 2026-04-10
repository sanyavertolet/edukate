package io.github.sanyavertolet.edukate.backend.storage

import io.github.sanyavertolet.edukate.backend.entities.files.FileObjectMetadata
import io.github.sanyavertolet.edukate.storage.AbstractStorage
import io.github.sanyavertolet.edukate.storage.configs.S3Properties
import io.github.sanyavertolet.edukate.storage.keys.FileKey
import io.github.sanyavertolet.edukate.storage.keys.fileKey
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Component
class FileKeyStorage(s3AsyncClient: S3AsyncClient, s3Presigner: S3Presigner, s3Properties: S3Properties) :
    AbstractStorage<FileKey, FileObjectMetadata>(s3AsyncClient, s3Presigner, s3Properties) {

    override fun buildKey(key: String): FileKey = fileKey(key)

    override fun buildMetadata(headObjectResponse: HeadObjectResponse): FileObjectMetadata =
        FileObjectMetadata(
            headObjectResponse.lastModified(),
            headObjectResponse.contentLength(),
            headObjectResponse.contentType(),
        )
}
