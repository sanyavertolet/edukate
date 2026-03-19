package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.common.SubmissionStatus
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("problem_status")
@CompoundIndex(name = "uniq_user_problem", def = "{ 'userId': 1, 'problemId': 1 }", unique = true)
data class UserProblemStatus(
    val userId: String,
    val problemId: String,
    val latestStatus: SubmissionStatus,
    val latestTime: Instant,
    val latestSubmissionId: String,
    val bestStatus: SubmissionStatus,
    val bestTime: Instant,
    val bestSubmissionId: String,
)
