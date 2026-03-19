package io.github.sanyavertolet.edukate.common.notifications

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import jakarta.validation.constraints.NotBlank
import java.util.UUID

@JsonTypeName("checked")
data class CheckedNotificationCreateRequest(
    override val uuid: String = UUID.randomUUID().toString(),
    @field:NotBlank override val targetUserId: String,
    @field:NotBlank val submissionId: String,
    @field:NotBlank val problemId: String,
    val status: CheckStatus,
) : BaseNotificationCreateRequest {
    companion object {
        @JvmStatic
        fun from(targetUserId: String, submissionId: String, problemId: String, status: CheckStatus) =
            CheckedNotificationCreateRequest(
                targetUserId = targetUserId,
                submissionId = submissionId,
                problemId = problemId,
                status = status,
            )
    }
}
