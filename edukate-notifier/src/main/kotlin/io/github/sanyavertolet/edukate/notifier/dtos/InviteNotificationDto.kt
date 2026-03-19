package io.github.sanyavertolet.edukate.notifier.dtos

import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.Instant

@JsonTypeName("invite")
data class InviteNotificationDto(
    override val uuid: String,
    override val isRead: Boolean,
    override val createdAt: Instant,
    val inviterName: String,
    val bundleName: String,
    val bundleShareCode: String,
) : BaseNotificationDto
