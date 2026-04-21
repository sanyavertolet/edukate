package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.common.SubmissionStatus
import java.time.Instant
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("problem_progress")
data class ProblemProgress(
    @Id val id: Long? = null,
    val userId: Long,
    val problemId: Long,
    val latestStatus: SubmissionStatus,
    val latestTime: Instant,
    val latestSubmissionId: Long,
    val bestStatus: SubmissionStatus,
    val bestTime: Instant,
    val bestSubmissionId: Long,
)
