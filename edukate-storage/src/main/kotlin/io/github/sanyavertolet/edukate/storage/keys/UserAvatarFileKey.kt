package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("avatar")
data class UserAvatarFileKey(val userId: Long) : FileKey {
    override val fileName: String = AVATAR_FILE_NAME

    override fun toString() = prefix(userId) + AVATAR_FILE_NAME

    override fun type() = "avatar"

    override fun owner() = userId

    companion object {
        const val AVATAR_FILE_NAME = "avatar.jpg"

        fun prefix(userId: Long) = "users/$userId/avatar/"
    }
}
