package io.github.sanyavertolet.edukate.common.checks

import io.github.sanyavertolet.edukate.common.ContentLanguage

data class SubmissionContext(
    val submissionId: Long,
    val checkResultId: Long,
    val problemId: Long,
    val problemText: String,
    val problemImageRawKeys: List<String>,
    val submissionImageRawKeys: List<String>,
    val answer: String? = null,
    val language: ContentLanguage = ContentLanguage.RU,
)
