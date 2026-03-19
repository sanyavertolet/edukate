package io.github.sanyavertolet.edukate.backend.dtos

import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import java.time.Instant

data class CheckResultDto(
    val status: CheckStatus,
    val trustLevel: Float,
    val errorType: CheckErrorType,
    val explanation: String,
    val createdAt: Instant,
)
