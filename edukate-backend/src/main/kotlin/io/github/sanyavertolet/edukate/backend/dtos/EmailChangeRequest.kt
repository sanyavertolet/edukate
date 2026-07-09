package io.github.sanyavertolet.edukate.backend.dtos

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class EmailChangeRequest(@field:NotBlank @field:Email val newEmail: String)
