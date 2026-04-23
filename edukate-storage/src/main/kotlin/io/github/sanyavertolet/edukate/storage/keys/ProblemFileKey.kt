package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("problem")
data class ProblemFileKey(val bookSlug: String, val problemCode: String, override val fileName: String) : FileKey {
    override fun toString() = prefix(bookSlug, problemCode) + fileName

    override fun type() = "problem"

    companion object {
        fun prefix(bookSlug: String, problemCode: String) = "books/$bookSlug/problems/$problemCode/"

        fun bookPrefix(bookSlug: String) = "books/$bookSlug/problems/"
    }
}
