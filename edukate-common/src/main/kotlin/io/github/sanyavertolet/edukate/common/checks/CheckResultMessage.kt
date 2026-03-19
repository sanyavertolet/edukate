package io.github.sanyavertolet.edukate.common.checks

data class CheckResultMessage (
    val submissionId: String,
    val status: CheckStatus,
    val trustLevel: Float,
    val errorType: CheckErrorType,
    val explanation: String
)
