package io.github.sanyavertolet.edukate.backend.dtos

import java.time.Instant

data class FileMetadata(
    val key: String,
    val authorName: String,
    val lastModified: Instant,
    val size: Long,
) {
    companion object {
        @JvmStatic
        fun of(
            key: String, authorName: String, lastModified: Instant, size: Long
        ) = FileMetadata(key, authorName, lastModified, size)
    }
}
