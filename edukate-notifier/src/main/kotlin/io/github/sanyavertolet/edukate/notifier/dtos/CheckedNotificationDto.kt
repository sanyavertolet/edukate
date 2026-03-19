package io.github.sanyavertolet.edukate.notifier.dtos

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import java.time.Instant

@JsonTypeName("checked")
data class CheckedNotificationDto(
    override val uuid: String,
    override val isRead: Boolean,
    override val createdAt: Instant,
    val submissionId: String,
    val problemId: String,
    val status: CheckStatus,
) : BaseNotificationDto
