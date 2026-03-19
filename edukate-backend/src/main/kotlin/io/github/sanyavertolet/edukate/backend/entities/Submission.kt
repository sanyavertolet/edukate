package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.common.SubmissionStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("submissions")
data class Submission(
    @field:Id
    val id: String? = null,
    val problemId: String,
    val userId: String,
    val status: SubmissionStatus,
    val fileObjectIds: List<String> = emptyList(),
    @field:CreatedDate
    val createdAt: Instant? = null,
) {
    constructor(problemId: String, userId: String) : this(
        problemId = problemId,
        userId = userId,
        status = SubmissionStatus.PENDING,
    )

    fun withStatus(status: SubmissionStatus): Submission = copy(status = status)

    fun withFileObjectIds(fileObjectIds: List<String>): Submission = copy(fileObjectIds = fileObjectIds.toList())

    companion object {
        @JvmStatic
        fun of(problemId: String, userId: String) = Submission(problemId, userId)
    }
}
