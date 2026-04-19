package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("result")
data class ResultFileKey(val problemId: Long, override val fileName: String) : FileKey {
    override fun toString() = prefix(problemId) + fileName

    override fun type() = "result"

    override fun owner(): Long? = null

    companion object {
        fun prefix(problemId: Long) = "results/${problemId.toString()}/"
    }
}
