package io.github.sanyavertolet.edukate.common;

import io.github.sanyavertolet.edukate.common.checks.CheckStatus;

public enum SubmissionStatus {
    PENDING,
    FAILED,
    SUCCESS,
    ;

    public static SubmissionStatus from(CheckStatus checkStatus) {
        if (checkStatus == CheckStatus.SUCCESS) {
            return SubmissionStatus.SUCCESS;
        } else if (checkStatus == CheckStatus.INTERNAL_ERROR) {
            return SubmissionStatus.FAILED;
        } else if (checkStatus == CheckStatus.MISTAKE) {
            return SubmissionStatus.FAILED;
        }
        return SubmissionStatus.PENDING;
    }

    public static SubmissionStatus best(SubmissionStatus lhs, SubmissionStatus rhs) {
        return lhs.compareTo(rhs) < 0 ? rhs : lhs;
    }
}
