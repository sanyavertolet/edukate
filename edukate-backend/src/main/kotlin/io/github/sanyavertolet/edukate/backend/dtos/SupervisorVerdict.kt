package io.github.sanyavertolet.edukate.backend.dtos

import io.github.sanyavertolet.edukate.backend.validators.AllowedCheckStatuses
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import jakarta.validation.constraints.NotBlank

data class SupervisorVerdict(
    @field:AllowedCheckStatuses(values = [CheckStatus.SUCCESS, CheckStatus.MISTAKE]) val status: CheckStatus,
    val errorType: CheckErrorType,
    @field:NotBlank val explanation: String,
)
