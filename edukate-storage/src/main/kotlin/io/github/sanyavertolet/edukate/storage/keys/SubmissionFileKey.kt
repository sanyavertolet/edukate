package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("submission")
data class SubmissionFileKey(
    val userId: String,
    val problemId: String,
    val submissionId: String,
    override val fileName: String,
) : FileKey {
    override fun toString() = prefix(userId, problemId, submissionId) + fileName

    override fun type() = "submission"

    override fun owner() = userId

    companion object {
        fun prefix(userId: String, problemId: String, submissionId: String) =
            "users/$userId/submissions/$problemId/$submissionId/"
    }
}
