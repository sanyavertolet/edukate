package io.github.sanyavertolet.edukate.backend.entities.files

import java.time.Instant

data class FileObjectMetadata(
    val lastModified: Instant,
    val contentLength: Long,
    val contentType: String,
)
