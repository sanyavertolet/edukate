package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName
import java.util.Objects

@JsonTypeName("submission")
class SubmissionFileKey(val userId: String, val problemId: String, val submissionId: String, fileName: String) :
    FileKey(fileName) {
    override fun equals(other: Any?) =
        other is SubmissionFileKey &&
            userId == other.userId &&
            problemId == other.problemId &&
            submissionId == other.submissionId

    override fun hashCode() = Objects.hash(userId, problemId, submissionId)

    override fun toString() = "users/$userId/submissions/$problemId/$submissionId/$fileName"

    companion object {
        @JvmStatic
        fun of(userId: String, problemId: String, submissionId: String, fileName: String) =
            SubmissionFileKey(userId, problemId, submissionId, fileName)

        @JvmStatic
        fun prefix(userId: String, problemId: String, submissionId: String) =
            "users/$userId/submissions/$problemId/$submissionId/"
    }
}
