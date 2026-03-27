package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("problem")
class ProblemFileKey(val problemId: String, fileName: String) : FileKey(fileName) {
    override fun equals(other: Any?) = other is ProblemFileKey && problemId == other.problemId

    override fun hashCode() = problemId.hashCode()

    override fun toString() = "problems/$problemId/$fileName"

    companion object {
        @JvmStatic fun of(problemId: String, fileName: String) = ProblemFileKey(problemId, fileName)

        @JvmStatic fun prefix(problemId: String) = "problems/$problemId/"
    }
}
