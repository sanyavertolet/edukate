package io.github.sanyavertolet.edukate.notifier.entities

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest
import io.github.sanyavertolet.edukate.notifier.dtos.InviteNotificationDto
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import java.time.Instant

@JsonTypeName("invite")
data class InviteNotification(
    @field:Id override val id: String? = null,
    @field:Indexed(unique = true) override val uuid: String,
    override val targetUserId: String,
    override val isRead: Boolean = false,
    @field:CreatedDate override val createdAt: Instant? = null,
    val inviterName: String,
    val bundleName: String,
    val bundleShareCode: String,
) : BaseNotification() {
    override fun toDto() =
        InviteNotificationDto(
            uuid = uuid,
            isRead = isRead,
            createdAt = requireNotNull(createdAt) { "createdAt is null for notification uuid=$uuid" },
            inviterName = inviterName,
            bundleName = bundleName,
            bundleShareCode = bundleShareCode,
        )

    companion object {
        @JvmStatic
        fun fromCreationRequest(creationRequest: InviteNotificationCreateRequest) =
            InviteNotification(
                uuid = creationRequest.uuid,
                targetUserId = creationRequest.targetUserId,
                inviterName = creationRequest.inviterName,
                bundleName = creationRequest.bundleName,
                bundleShareCode = creationRequest.bundleShareCode,
            )
    }
}
