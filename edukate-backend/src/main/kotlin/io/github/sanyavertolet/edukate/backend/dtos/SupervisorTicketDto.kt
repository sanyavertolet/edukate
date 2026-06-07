package io.github.sanyavertolet.edukate.backend.dtos

import io.github.sanyavertolet.edukate.common.checks.SupervisorTicketStatus
import java.time.Instant

data class SupervisorTicketDto(
    val id: Long,
    val problemKey: String,
    val problemSetShareCode: String,
    val fileUrls: List<String>,
    val status: SupervisorTicketStatus,
    val createdAt: Instant,
)
