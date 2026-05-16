package io.github.sanyavertolet.edukate.common.notifications

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = EmailVerificationMessage::class, name = "verification"),
    JsonSubTypes.Type(value = PasswordResetMessage::class, name = "password_reset"),
)
sealed interface BaseEmailMessage {
    val toEmail: String
    val token: UUID
    val username: String
}
