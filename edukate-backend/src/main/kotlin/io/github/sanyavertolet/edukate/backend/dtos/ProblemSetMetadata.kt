package io.github.sanyavertolet.edukate.backend.dtos

data class ProblemSetMetadata(
    val name: String,
    val description: String?,
    val admins: List<String>,
    val shareCode: String,
    val isPublic: Boolean,
    val size: Long,
    val solvedCount: Long,
)
