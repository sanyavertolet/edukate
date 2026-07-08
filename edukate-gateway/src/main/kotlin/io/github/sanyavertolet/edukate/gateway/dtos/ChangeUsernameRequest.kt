package io.github.sanyavertolet.edukate.gateway.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChangeUsernameRequest(@field:NotBlank @field:Size(min = 3, max = 32) val newUsername: String)
