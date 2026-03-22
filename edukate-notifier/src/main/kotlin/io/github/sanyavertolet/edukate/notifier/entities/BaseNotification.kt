package io.github.sanyavertolet.edukate.notifier.entities

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.CheckedNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest
import io.github.sanyavertolet.edukate.notifier.dtos.BaseNotificationDto
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SimpleNotification::class, name = "simple"),
    JsonSubTypes.Type(value = InviteNotification::class, name = "invite"),
    JsonSubTypes.Type(value = CheckedNotification::class, name = "checked"),
)
@Document("notifications")
sealed class BaseNotification {
    abstract val id: String?
    abstract val uuid: String
    abstract val targetUserId: String
    abstract val isRead: Boolean
    abstract val createdAt: Instant?

    abstract fun toDto(): BaseNotificationDto

    fun markAsRead(): BaseNotification =
        when (this) {
            is SimpleNotification -> copy(isRead = true)
            is InviteNotification -> copy(isRead = true)
            is CheckedNotification -> copy(isRead = true)
        }

    companion object {
        @JvmStatic
        fun fromCreationRequest(request: BaseNotificationCreateRequest) =
            when (request) {
                is SimpleNotificationCreateRequest -> SimpleNotification.fromCreationRequest(request)
                is InviteNotificationCreateRequest -> InviteNotification.fromCreationRequest(request)
                is CheckedNotificationCreateRequest -> CheckedNotification.fromCreationRequest(request)
            }
    }
}
