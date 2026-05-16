package io.github.sanyavertolet.edukate.gateway.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ResetPasswordRequest(
    @field:NotBlank val token: String,
    @field:NotBlank @field:Size(min = 6, max = 128) val newPassword: String,
)
