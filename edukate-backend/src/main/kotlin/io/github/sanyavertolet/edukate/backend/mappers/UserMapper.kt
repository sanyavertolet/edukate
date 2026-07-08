package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.dtos.UserDto
import io.github.sanyavertolet.edukate.backend.dtos.UserInfoDto
import io.github.sanyavertolet.edukate.backend.entities.User
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.storage.configs.S3Properties
import io.github.sanyavertolet.edukate.storage.keys.UserAvatarFileKey
import org.springframework.stereotype.Component

@Component
class UserMapper(private val s3Properties: S3Properties) {
    fun toDto(user: User): UserDto =
        UserDto(
            name = user.name,
            email = user.email,
            roles = user.roles.map(UserRole::name),
            status = user.status.name,
            avatarUrl = avatarUrl(user),
        )

    fun toInfoDto(user: User): UserInfoDto = UserInfoDto(name = user.name, avatarUrl = avatarUrl(user))

    fun avatarUrl(user: User): String? {
        val updatedAt = user.avatarUpdatedAt ?: return null
        val userId = user.id ?: return null
        val host = s3Properties.publicEndpoint ?: s3Properties.endpoint
        return "$host/${s3Properties.bucket}/${UserAvatarFileKey.prefix(userId)}${UserAvatarFileKey.AVATAR_FILE_NAME}" +
            "?v=${updatedAt.epochSecond}"
    }
}
