package io.github.sanyavertolet.edukate.backend.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class CreateSubmissionRequest(
    @field:NotBlank val problemId: String,
    @field:NotEmpty val fileNames: List<@NotBlank String>,
)
