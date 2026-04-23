package io.github.sanyavertolet.edukate.checker.utils

import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.github.sanyavertolet.edukate.common.checks.SubmissionContext

fun success(modelResponse: ModelResponse, submissionContext: SubmissionContext): CheckResultMessage =
    CheckResultMessage(
        submissionContext.submissionId,
        submissionContext.checkResultId,
        modelResponse.status,
        modelResponse.trustLevel.coerceIn(0f, 1f),
        if (modelResponse.status == CheckStatus.SUCCESS) CheckErrorType.NONE else modelResponse.errorType,
        modelResponse.explanation,
    )

fun error(submissionContext: SubmissionContext): CheckResultMessage =
    CheckResultMessage(
        submissionContext.submissionId,
        submissionContext.checkResultId,
        CheckStatus.INTERNAL_ERROR,
        0f,
        CheckErrorType.NONE,
        "Automatic check failed. Please retry later or contact support.",
    )
