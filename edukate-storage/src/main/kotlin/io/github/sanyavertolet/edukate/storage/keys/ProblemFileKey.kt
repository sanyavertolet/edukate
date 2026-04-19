package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("problem")
data class ProblemFileKey(val problemId: Long, override val fileName: String) : FileKey {
    override fun toString() = prefix(problemId) + fileName

    override fun type() = "problem"

    override fun owner(): Long? = null

    companion object {
        fun prefix(problemId: Long) = "problems/${problemId.toString()}/"
    }
}
