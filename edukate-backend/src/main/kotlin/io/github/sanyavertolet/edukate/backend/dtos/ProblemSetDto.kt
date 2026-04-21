package io.github.sanyavertolet.edukate.backend.dtos

data class ProblemSetDto(
    val name: String,
    val description: String?,
    val admins: List<String>,
    val isPublic: Boolean,
    val problems: List<ProblemMetadata>,
    val shareCode: String,
)
