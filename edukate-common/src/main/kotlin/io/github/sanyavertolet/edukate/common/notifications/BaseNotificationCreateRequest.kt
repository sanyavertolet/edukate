package io.github.sanyavertolet.edukate.common.notifications

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SimpleNotificationCreateRequest::class, name = "simple"),
    JsonSubTypes.Type(value = InviteNotificationCreateRequest::class, name = "invite"),
    JsonSubTypes.Type(value = CheckedNotificationCreateRequest::class, name = "checked"),
)
sealed interface BaseNotificationCreateRequest {
    val uuid: String
    val targetUserId: String
}
