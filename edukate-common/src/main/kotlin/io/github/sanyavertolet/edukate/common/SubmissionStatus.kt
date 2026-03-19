package io.github.sanyavertolet.edukate.common

import io.github.sanyavertolet.edukate.common.checks.CheckStatus

enum class SubmissionStatus {
    PENDING,
    FAILED,
    SUCCESS,
    ;

    companion object {
        @JvmStatic
        fun from(checkStatus: CheckStatus?) = when (checkStatus) {
            CheckStatus.SUCCESS -> SUCCESS
            CheckStatus.INTERNAL_ERROR -> FAILED
            CheckStatus.MISTAKE -> FAILED
            else -> PENDING
        }

        @JvmStatic
        fun best(lhs: SubmissionStatus, rhs: SubmissionStatus): SubmissionStatus = if (lhs < rhs) rhs else lhs
    }
}
