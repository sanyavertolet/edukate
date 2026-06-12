package io.github.sanyavertolet.edukate.backend.dtos

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size

data class UpdateProblemSetSettingsRequest(
    @field:Size(min = 1, max = 50) val name: String? = null,
    @field:Size(max = 255) val description: String? = null,
    val isPublic: Boolean? = null,
    val problemKeys: List<String>? = null,
) {
    @Schema(hidden = true)
    @AssertTrue(message = "At least one field must be provided")
    fun isNotEmpty(): Boolean = name != null || description != null || isPublic != null || problemKeys != null

    @Schema(hidden = true)
    @AssertTrue(message = "problemKeys must contain at least one item when present")
    fun isProblemKeysNonEmpty(): Boolean = problemKeys == null || problemKeys.isNotEmpty()
}
