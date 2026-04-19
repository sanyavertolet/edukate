package io.github.sanyavertolet.edukate.notifier.entities

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest
import io.github.sanyavertolet.edukate.notifier.dtos.InviteNotificationDto
import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed

@JsonTypeName("invite")
data class InviteNotification(
    @field:Id override val id: String? = null,
    @field:Indexed(unique = true) override val uuid: String,
    override val targetUserId: Long,
    override val isRead: Boolean = false,
    @field:CreatedDate override val createdAt: Instant? = null,
    val inviterName: String,
    val problemSetName: String,
    val problemSetShareCode: String,
) : BaseNotification() {
    override fun toDto() =
        InviteNotificationDto(
            uuid = uuid,
            isRead = isRead,
            createdAt = requireNotNull(createdAt) { "createdAt is null for notification uuid=$uuid" },
            inviterName = inviterName,
            problemSetName = problemSetName,
            problemSetShareCode = problemSetShareCode,
        )

    override fun markAsRead() = copy(isRead = true)

    companion object {
        @JvmStatic
        fun fromCreationRequest(creationRequest: InviteNotificationCreateRequest) =
            InviteNotification(
                uuid = creationRequest.uuid,
                targetUserId = creationRequest.targetUserId,
                inviterName = creationRequest.inviterName,
                problemSetName = creationRequest.problemSetName,
                problemSetShareCode = creationRequest.problemSetShareCode,
            )
    }
}
