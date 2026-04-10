package io.github.sanyavertolet.edukate.notifier.entities

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.github.sanyavertolet.edukate.common.notifications.CheckedNotificationCreateRequest
import io.github.sanyavertolet.edukate.notifier.dtos.CheckedNotificationDto
import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed

@JsonTypeName("checked")
data class CheckedNotification(
    @field:Id override val id: String? = null,
    @field:Indexed(unique = true) override val uuid: String,
    override val targetUserId: String,
    override val isRead: Boolean = false,
    @field:CreatedDate override val createdAt: Instant? = null,
    val submissionId: String,
    val problemId: String,
    val status: CheckStatus,
) : BaseNotification() {
    override fun toDto() =
        CheckedNotificationDto(
            uuid = uuid,
            isRead = isRead,
            createdAt = requireNotNull(createdAt) { "createdAt is null for notification uuid=$uuid" },
            submissionId = submissionId,
            problemId = problemId,
            status = status,
        )

    override fun markAsRead() = copy(isRead = true)

    companion object {
        @JvmStatic
        fun fromCreationRequest(creationRequest: CheckedNotificationCreateRequest) =
            CheckedNotification(
                uuid = creationRequest.uuid,
                targetUserId = creationRequest.targetUserId,
                submissionId = creationRequest.submissionId,
                problemId = creationRequest.problemId,
                status = creationRequest.status,
            )
    }
}
