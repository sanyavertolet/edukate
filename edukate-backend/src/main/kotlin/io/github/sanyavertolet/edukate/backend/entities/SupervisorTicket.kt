package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.common.checks.SupervisorTicketStatus
import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("supervisor_tickets")
data class SupervisorTicket(
    @Id val id: Long? = null,
    val submissionId: Long,
    val problemSetId: Long,
    val supervisorId: Long,
    val checkResultId: Long,
    val status: SupervisorTicketStatus = SupervisorTicketStatus.PENDING,
    @CreatedDate val createdAt: Instant? = null,
)
