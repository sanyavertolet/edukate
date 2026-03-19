package io.github.sanyavertolet.edukate.checker.utils;

import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse;
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType;
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage;
import io.github.sanyavertolet.edukate.common.checks.CheckStatus;
import io.github.sanyavertolet.edukate.common.checks.SubmissionContext;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CheckResultMessageUtils {

    public static CheckResultMessage success(
            @NonNull ModelResponse modelResponse,
            @NonNull SubmissionContext submissionContext
    ) {
        return new CheckResultMessage(
                submissionContext.getSubmissionId(),
                modelResponse.getStatus(),
                Math.max(
                        0f,
                        Math.min(1f, modelResponse.getTrustLevel())
                ),
                modelResponse.getStatus() == CheckStatus.SUCCESS
                        ? CheckErrorType.NONE
                        : modelResponse.getErrorType(),
                modelResponse.getExplanation()
        );
    }

    public static CheckResultMessage error(@NonNull SubmissionContext submissionContext) {
        return new CheckResultMessage(
                submissionContext.getSubmissionId(),
                CheckStatus.INTERNAL_ERROR,
                0f,
                CheckErrorType.NONE,
                "Automatic check failed. Please retry later or contact support."
        );
    }
}
