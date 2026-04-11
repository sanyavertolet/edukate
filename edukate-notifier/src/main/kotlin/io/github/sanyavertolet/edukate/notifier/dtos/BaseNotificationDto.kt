package io.github.sanyavertolet.edukate.notifier.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant

@JsonPropertyOrder("createdAt", "uuid", "isRead", "_type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SimpleNotificationDto::class, name = "simple"),
    JsonSubTypes.Type(value = InviteNotificationDto::class, name = "invite"),
    JsonSubTypes.Type(value = CheckedNotificationDto::class, name = "checked"),
)
sealed interface BaseNotificationDto {
    val uuid: String

    @get:JsonProperty("isRead") val isRead: Boolean
    val createdAt: Instant
}
