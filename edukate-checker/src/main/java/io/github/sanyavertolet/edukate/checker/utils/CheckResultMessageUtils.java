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
        return CheckResultMessage.builder()
                .submissionId(submissionContext.getSubmissionId())
                .status(modelResponse.getStatus())
                .trustLevel(Math.max(
                        0f,
                        Math.min(1f, modelResponse.getTrustLevel())
                ))
                .errorType(modelResponse.getStatus() == CheckStatus.SUCCESS
                        ? CheckErrorType.NONE
                        : modelResponse.getErrorType()
                )
                .explanation(modelResponse.getExplanation())
                .build();
    }

    public static CheckResultMessage error(@NonNull SubmissionContext submissionContext) {
        return CheckResultMessage.builder()
                .submissionId(submissionContext.getSubmissionId())
                .errorType(CheckErrorType.NONE)
                .status(CheckStatus.INTERNAL_ERROR)
                .explanation("Automatic check failed. Please retry later or contact support.")
                .trustLevel(0f)
                .build()
                ;
    }
}
