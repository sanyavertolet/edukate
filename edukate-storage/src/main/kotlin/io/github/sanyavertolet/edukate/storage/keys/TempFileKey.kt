package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("tmp")
data class TempFileKey(val userId: String, override val fileName: String) : FileKey {
    override fun toString() = prefix(userId) + fileName

    override fun type() = "tmp"

    override fun owner() = userId

    companion object {
        fun prefix(userId: String) = "users/$userId/tmp/"
    }
}
