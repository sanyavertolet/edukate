package io.github.sanyavertolet.edukate.checker

import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.github.sanyavertolet.edukate.common.checks.SubmissionContext
import org.springframework.ai.content.Media
import org.springframework.http.MediaType

object CheckerFixtures {
    fun submissionContext(
        submissionId: Long = 1L,
        checkResultId: Long = 10L,
        problemId: Long = 1L,
        problemText: String = "Solve x^2 = 4",
        problemImageRawKeys: List<String> = listOf("problems/prob-1/img.png"),
        submissionImageRawKeys: List<String> = listOf("users/u1/submissions/prob-1/sub-1/img.png"),
    ) = SubmissionContext(submissionId, checkResultId, problemId, problemText, problemImageRawKeys, submissionImageRawKeys)

    fun modelResponse(
        status: CheckStatus = CheckStatus.SUCCESS,
        trustLevel: Float = 0.9f,
        errorType: CheckErrorType = CheckErrorType.NONE,
        explanation: String = "Correct.",
    ) = ModelResponse(status, trustLevel, errorType, explanation)

    fun checkResultMessage(
        submissionId: Long = 1L,
        checkResultId: Long = 10L,
        status: CheckStatus = CheckStatus.SUCCESS,
        trustLevel: Float = 0.9f,
        errorType: CheckErrorType = CheckErrorType.NONE,
        explanation: String = "Correct.",
    ) = CheckResultMessage(submissionId, checkResultId, status, trustLevel, errorType, explanation)

    fun mockMedia(): Media = Media.builder().mimeType(MediaType.IMAGE_PNG).data("test".toByteArray()).build()
}
