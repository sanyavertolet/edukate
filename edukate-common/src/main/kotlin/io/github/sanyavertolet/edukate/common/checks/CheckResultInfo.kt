package io.github.sanyavertolet.edukate.common.checks

import java.time.Instant

data class CheckResultInfo(val id: String, val status: CheckStatus, val trustLevel: Float, val createdAt: Instant)
