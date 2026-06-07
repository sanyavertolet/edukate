package io.github.sanyavertolet.edukate.backend.dtos

data class CreateSupervisorTicketRequest(val submissionId: Long, val problemSetCode: String, val supervisorName: String)
