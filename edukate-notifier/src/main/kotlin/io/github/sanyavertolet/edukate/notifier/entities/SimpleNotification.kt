package io.github.sanyavertolet.edukate.notifier.entities

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest
import io.github.sanyavertolet.edukate.notifier.dtos.SimpleNotificationDto
import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed

@JsonTypeName("simple")
data class SimpleNotification(
    @field:Id override val id: String? = null,
    @field:Indexed(unique = true) override val uuid: String,
    override val targetUserId: Long,
    override val isRead: Boolean = false,
    @field:CreatedDate override val createdAt: Instant? = null,
    val title: String,
    val message: String,
    val source: String,
) : BaseNotification() {
    override fun toDto() =
        SimpleNotificationDto(
            uuid = uuid,
            isRead = isRead,
            createdAt = requireNotNull(createdAt) { "createdAt is null for notification uuid=$uuid" },
            title = title,
            message = message,
            source = source,
        )

    override fun markAsRead() = copy(isRead = true)

    companion object {
        @JvmStatic
        fun fromCreationRequest(creationRequest: SimpleNotificationCreateRequest) =
            SimpleNotification(
                uuid = creationRequest.uuid,
                targetUserId = creationRequest.targetUserId,
                title = creationRequest.title,
                message = creationRequest.message,
                source = creationRequest.source,
            )
    }
}
