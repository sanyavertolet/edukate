package io.github.sanyavertolet.edukate.backend.permissions

import io.github.sanyavertolet.edukate.backend.entities.Submission
import org.springframework.stereotype.Component

@Component
class SubmissionPermissionEvaluator {
    fun isOwner(submission: Submission, userId: String): Boolean = submission.userId == userId
}
