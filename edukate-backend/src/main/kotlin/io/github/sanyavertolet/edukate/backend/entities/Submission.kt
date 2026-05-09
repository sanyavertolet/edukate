package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.common.ContentLanguage
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table

@Table("submissions")
data class Submission(
    @Id val id: Long? = null,
    val problemId: Long,
    val userId: Long,
    val status: SubmissionStatus,
    val language: ContentLanguage = ContentLanguage.RU,
    val fileObjectIds: List<String> = emptyList(),
    @CreatedDate val createdAt: Instant? = null,
    @LastModifiedDate val updatedAt: Instant? = null,
) {
    fun withFileObjectIds(fileObjectIds: List<String>): Submission = copy(fileObjectIds = fileObjectIds.toList())

    companion object {
        @JvmStatic
        fun of(problemId: Long, userId: Long, language: ContentLanguage = ContentLanguage.RU) =
            Submission(problemId = problemId, userId = userId, status = SubmissionStatus.PENDING, language = language)
    }
}
