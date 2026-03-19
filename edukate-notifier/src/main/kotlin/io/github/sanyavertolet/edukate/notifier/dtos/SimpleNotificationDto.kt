package io.github.sanyavertolet.edukate.notifier.dtos

import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.Instant

@JsonTypeName("simple")
data class SimpleNotificationDto(
    override val uuid: String,
    override val isRead: Boolean,
    override val createdAt: Instant,
    val title: String,
    val message: String,
    val source: String,
) : BaseNotificationDto
