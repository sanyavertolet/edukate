package io.github.sanyavertolet.edukate.common.notifications

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import java.util.UUID

@JsonTypeName("checked")
data class CheckedNotificationCreateRequest(
    override val uuid: String = UUID.randomUUID().toString(),
    override val targetUserId: Long,
    val submissionId: Long,
    val problemKey: String,
    val status: CheckStatus,
) : BaseNotificationCreateRequest {
    companion object {
        @JvmStatic
        fun from(targetUserId: Long, submissionId: Long, problemKey: String, status: CheckStatus) =
            CheckedNotificationCreateRequest(
                targetUserId = targetUserId,
                submissionId = submissionId,
                problemKey = problemKey,
                status = status,
            )
    }
}
