package io.github.sanyavertolet.edukate.backend.filters

import io.github.sanyavertolet.edukate.common.SubmissionStatus

data class SubmissionFilter(
    val userPrefix: String? = null,
    val bookSlugPrefix: String? = null,
    val problemCodePrefix: String? = null,
    val status: SubmissionStatus? = null,
)
