package io.github.sanyavertolet.edukate.backend.dtos

data class UserDto(
    val name: String,
    val email: String,
    val roles: List<String>,
    val status: String,
    val avatarUrl: String? = null,
)
