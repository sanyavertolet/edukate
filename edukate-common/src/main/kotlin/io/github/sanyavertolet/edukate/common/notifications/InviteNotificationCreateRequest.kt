package io.github.sanyavertolet.edukate.common.notifications

import com.fasterxml.jackson.annotation.JsonTypeName
import jakarta.validation.constraints.NotBlank
import java.util.UUID

@JsonTypeName("invite")
data class InviteNotificationCreateRequest(
    override val uuid: String = UUID.randomUUID().toString(),
    @field:NotBlank override val targetUserId: String,
    @field:NotBlank val inviterName: String,
    @field:NotBlank val bundleName: String,
    @field:NotBlank val bundleShareCode: String,
) : BaseNotificationCreateRequest {
    companion object {
        @JvmStatic
        fun from(targetUserId: String, inviterName: String, bundleName: String, bundleShareCode: String) =
            InviteNotificationCreateRequest(
                targetUserId = targetUserId,
                inviterName = inviterName,
                bundleName = bundleName,
                bundleShareCode = bundleShareCode,
            )
    }
}
