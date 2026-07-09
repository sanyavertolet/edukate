package io.github.sanyavertolet.edukate.gateway.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(
    @field:NotBlank val currentPassword: String,
    @field:NotBlank @field:Size(min = 6, max = 20) val newPassword: String,
)
