package io.github.sanyavertolet.edukate.common.checks

data class SubmissionContext(
    val submissionId: Long,
    val checkResultId: Long,
    val problemId: Long,
    val problemText: String,
    val problemImageRawKeys: List<String>,
    val submissionImageRawKeys: List<String>,
)
