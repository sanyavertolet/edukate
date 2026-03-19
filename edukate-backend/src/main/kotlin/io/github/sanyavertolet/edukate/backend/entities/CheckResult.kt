package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.backend.dtos.CheckResultDto
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckResultInfo
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("check_results")
data class CheckResult(
    @field:Id
    val id: String? = null,
    @field:Indexed
    val submissionId: String,
    val status: CheckStatus,
    val trustLevel: Float,
    val errorType: CheckErrorType,
    val explanation: String,
    @field:CreatedDate
    val createdAt: Instant? = null,
) {
    fun toCheckResultDto() =
        CheckResultDto(
            status,
            trustLevel,
            errorType,
            explanation,
            requireNotNull(createdAt) { "Initialized CheckResult createdAt cannot be null" },
        )

    fun toCheckResultInfo() =
        CheckResultInfo(
            requireNotNull(id) { "Initialized CheckResult ID cannot be null" },
            status,
            trustLevel,
            requireNotNull(createdAt) { "Initialized CheckResult createdAt cannot be null" },
        )

    companion object {
        @JvmStatic
        fun self(submissionId: String) =
            CheckResult(
                submissionId = submissionId,
                status = CheckStatus.SUCCESS,
                trustLevel = 0.01f,
                errorType = CheckErrorType.NONE,
                explanation = "User considered this problem as solved.",
            )

        @JvmStatic
        fun fromCheckResultMessage(checkResultMessage: CheckResultMessage) =
            CheckResult(
                submissionId = checkResultMessage.submissionId,
                status = checkResultMessage.status,
                trustLevel = checkResultMessage.trustLevel,
                errorType = checkResultMessage.errorType,
                explanation = checkResultMessage.explanation,
            )
    }
}
