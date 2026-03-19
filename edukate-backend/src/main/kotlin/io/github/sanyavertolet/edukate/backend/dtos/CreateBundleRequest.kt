package io.github.sanyavertolet.edukate.backend.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class CreateBundleRequest(
    @field:NotBlank @field:Size(max = 50) val name: String,
    @field:NotBlank @field:Size(max = 255) val description: String,
    val isPublic: Boolean,
    @field:NotEmpty val problemIds: List<@NotBlank String>,
)
