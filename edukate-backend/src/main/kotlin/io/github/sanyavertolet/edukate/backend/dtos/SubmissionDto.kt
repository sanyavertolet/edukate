package io.github.sanyavertolet.edukate.backend.dtos

import io.github.sanyavertolet.edukate.common.SubmissionStatus
import java.time.Instant

data class SubmissionDto(
    val id: String,
    val problemId: String,
    val userName: String,
    val status: SubmissionStatus,
    val createdAt: Instant,
    val fileUrls: List<String> = emptyList(),
)
