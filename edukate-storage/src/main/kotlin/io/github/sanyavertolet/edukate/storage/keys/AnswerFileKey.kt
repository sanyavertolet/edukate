package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("answer")
data class AnswerFileKey(val bookSlug: String, val problemCode: String, override val fileName: String) : FileKey {
    override fun toString() = prefix(bookSlug, problemCode) + fileName

    override fun type() = "answer"

    companion object {
        fun prefix(bookSlug: String, problemCode: String) = "books/$bookSlug/answers/$problemCode/"
    }
}
