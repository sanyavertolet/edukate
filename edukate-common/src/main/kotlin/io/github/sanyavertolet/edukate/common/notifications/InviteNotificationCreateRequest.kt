package io.github.sanyavertolet.edukate.common.notifications

import com.fasterxml.jackson.annotation.JsonTypeName
import jakarta.validation.constraints.NotBlank
import java.util.UUID

@JsonTypeName("invite")
data class InviteNotificationCreateRequest(
    override val uuid: String = UUID.randomUUID().toString(),
    override val targetUserId: Long,
    @field:NotBlank val inviterName: String,
    @field:NotBlank val problemSetName: String,
    @field:NotBlank val problemSetShareCode: String,
) : BaseNotificationCreateRequest {
    companion object {
        @JvmStatic
        fun from(targetUserId: Long, inviterName: String, problemSetName: String, problemSetShareCode: String) =
            InviteNotificationCreateRequest(
                targetUserId = targetUserId,
                inviterName = inviterName,
                problemSetName = problemSetName,
                problemSetShareCode = problemSetShareCode,
            )
    }
}
