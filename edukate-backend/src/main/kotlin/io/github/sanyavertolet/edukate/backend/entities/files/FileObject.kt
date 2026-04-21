package io.github.sanyavertolet.edukate.backend.entities.files

import io.github.sanyavertolet.edukate.storage.keys.FileKey
import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table

@Table("file_objects")
data class FileObject(
    @Id val id: Long? = null,
    val keyPath: String,
    val key: FileKey,
    val type: String,
    val ownerUserId: Long,
    val metadata: FileObjectMetadata,
    @CreatedDate val createdAt: Instant? = null,
    @LastModifiedDate val updatedAt: Instant? = null,
    val metaVersion: Int = 1,
) {
    fun withStorageState(
        keyPath: String,
        key: FileKey,
        type: String,
        ownerUserId: Long,
        metadata: FileObjectMetadata,
    ): FileObject = copy(keyPath = keyPath, key = key, type = type, ownerUserId = ownerUserId, metadata = metadata)

    companion object {
        @JvmStatic
        fun fromStorageState(keyPath: String, key: FileKey, type: String, ownerUserId: Long, metadata: FileObjectMetadata) =
            FileObject(keyPath = keyPath, key = key, type = type, ownerUserId = ownerUserId, metadata = metadata)
    }
}
