package io.github.sanyavertolet.edukate.backend.entities.files

import io.github.sanyavertolet.edukate.storage.keys.FileKey
import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("file_objects")
data class FileObject(
    @field:Id val id: String? = null,
    @field:Indexed(unique = true) val keyPath: String,
    val key: FileKey,
    val type: String,
    val ownerUserId: String,
    val metadata: FileObjectMetadata,
    @field:CreatedDate val createdAt: Instant? = null,
    @field:LastModifiedDate val updatedAt: Instant? = null,
    val metaVersion: Int = 1,
) {
    @Suppress("DataClassContainsFunctions")
    fun withStorageState(
        keyPath: String,
        key: FileKey,
        type: String,
        ownerUserId: String,
        metadata: FileObjectMetadata,
    ): FileObject = copy(keyPath = keyPath, key = key, type = type, ownerUserId = ownerUserId, metadata = metadata)

    companion object {
        @JvmStatic
        fun fromStorageState(
            keyPath: String,
            key: FileKey,
            type: String,
            ownerUserId: String,
            metadata: FileObjectMetadata,
        ) = FileObject(keyPath = keyPath, key = key, type = type, ownerUserId = ownerUserId, metadata = metadata)
    }
}
