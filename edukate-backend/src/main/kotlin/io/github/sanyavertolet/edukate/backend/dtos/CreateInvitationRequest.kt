package io.github.sanyavertolet.edukate.backend.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateInvitationRequest(@field:NotBlank @field:Size(max = 50) val inviteeName: String)
