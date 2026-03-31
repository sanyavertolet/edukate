package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("result")
data class ResultFileKey(val problemId: String, override val fileName: String) : FileKey {
    override fun toString() = prefix(problemId) + fileName

    override fun type() = "result"

    override fun owner(): String? = null

    companion object {
        fun prefix(problemId: String) = "results/$problemId/"
    }
}
