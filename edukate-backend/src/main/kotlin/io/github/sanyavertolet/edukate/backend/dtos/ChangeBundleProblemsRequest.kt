package io.github.sanyavertolet.edukate.backend.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class ChangeBundleProblemsRequest(@field:NotEmpty val problemIds: List<@NotBlank String>)
