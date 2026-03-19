package io.github.sanyavertolet.edukate.backend.dtos

import io.github.sanyavertolet.edukate.backend.entities.User
import io.github.sanyavertolet.edukate.common.users.UserRole

data class UserDto(
    val name: String,
    val roles: List<String>,
    val status: String,
) {
    companion object {
        @JvmStatic
        fun of(user: User): UserDto = UserDto(
            user.name,
            user.roles.map(UserRole::name),
            user.status.name,
        )
    }
}
