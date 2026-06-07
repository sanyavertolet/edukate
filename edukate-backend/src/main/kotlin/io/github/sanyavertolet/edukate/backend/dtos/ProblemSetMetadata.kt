package io.github.sanyavertolet.edukate.backend.dtos

import io.github.sanyavertolet.edukate.common.users.UserRole

data class ProblemSetMetadata(
    val name: String,
    val description: String?,
    val admins: List<String>,
    val shareCode: String,
    val isPublic: Boolean,
    val size: Long,
    val solvedCount: Long,
    val currentUserRole: UserRole? = null,
)
