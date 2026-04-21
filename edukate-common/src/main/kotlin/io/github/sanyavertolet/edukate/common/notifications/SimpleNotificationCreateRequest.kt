package io.github.sanyavertolet.edukate.common.notifications

import com.fasterxml.jackson.annotation.JsonTypeName
import jakarta.validation.constraints.NotBlank
import java.util.UUID

@JsonTypeName("simple")
data class SimpleNotificationCreateRequest(
    override val uuid: String = UUID.randomUUID().toString(),
    override val targetUserId: Long,
    @field:NotBlank val title: String,
    @field:NotBlank val message: String,
    @field:NotBlank val source: String,
) : BaseNotificationCreateRequest
