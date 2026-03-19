package io.github.sanyavertolet.edukate.checker.dtos

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckStatus

data class ModelResponse(
    @field:JsonPropertyDescription(
        "Result of the automated check: 'SUCCESS' when the submission is correct; " +
            "'MISTAKE' when at least one error was detected. This field MUST be either SUCCESS or MISTAKE."
    )
    val status: CheckStatus = CheckStatus.INTERNAL_ERROR,
    @field:JsonPropertyDescription(
        "Model confidence in the check result, from 0.0 (no confidence) to 1.0 (highest confidence)."
    )
    val trustLevel: Float = 0f,
    @field:JsonPropertyDescription(
        "Type of the detected error, or 'NONE' when no error is present. Must be 'NONE' when status is 'SUCCESS'."
    )
    val errorType: CheckErrorType = CheckErrorType.NONE,
    @field:JsonPropertyDescription(
        "Human-readable explanation: for a mistake, describes what is wrong and (if possible) where;" +
            "for a correct solution, a short confirmation or brief rationale."
    )
    val explanation: String = "",
)
