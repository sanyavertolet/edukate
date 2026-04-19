package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("submission")
data class SubmissionFileKey(val userId: Long, val problemId: Long, val submissionId: Long, override val fileName: String) :
    FileKey {
    override fun toString() = prefix(userId, problemId, submissionId) + fileName

    override fun type() = "submission"

    override fun owner() = userId

    companion object {
        fun prefix(userId: Long, problemId: Long, submissionId: Long) =
            "users/${userId.toString()}/submissions/${problemId.toString()}/${submissionId.toString()}/"
    }
}
