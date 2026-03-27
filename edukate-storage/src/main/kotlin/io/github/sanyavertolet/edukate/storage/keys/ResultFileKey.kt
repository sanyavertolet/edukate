package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("result")
class ResultFileKey(val problemId: String, fileName: String) : FileKey(fileName) {
    override fun equals(other: Any?) = other is ResultFileKey && problemId == other.problemId

    override fun hashCode() = problemId.hashCode()

    override fun toString() = "results/$problemId/$fileName"

    companion object {
        @JvmStatic fun of(problemId: String, fileName: String) = ResultFileKey(problemId, fileName)

        @JvmStatic fun prefix(problemId: String) = "results/$problemId/"
    }
}
