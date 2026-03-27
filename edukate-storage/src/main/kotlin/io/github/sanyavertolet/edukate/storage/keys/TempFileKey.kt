package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("tmp")
class TempFileKey(val userId: String, fileName: String) : FileKey(fileName) {
    override fun equals(other: Any?) = other is TempFileKey && userId == other.userId

    override fun hashCode() = userId.hashCode()

    override fun toString() = "users/$userId/tmp/$fileName"

    companion object {
        @JvmStatic fun of(userId: String, fileName: String) = TempFileKey(userId, fileName)

        @JvmStatic fun prefix(userId: String) = "users/$userId/tmp/"
    }
}
