package io.github.sanyavertolet.edukate.common.checks

data class SubmissionContext(
    val submissionId: String,
    val problemId: String,
    val problemText: String,
    val problemImageRawKeys: List<String>,
    val submissionImageRawKeys: List<String>,
)
