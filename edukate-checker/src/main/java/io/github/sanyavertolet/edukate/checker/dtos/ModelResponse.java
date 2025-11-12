package io.github.sanyavertolet.edukate.checker.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType;
import io.github.sanyavertolet.edukate.common.checks.CheckResult;
import io.github.sanyavertolet.edukate.common.checks.CheckStatus;
import lombok.Data;

@Data
public class ModelResponse {
    @JsonPropertyDescription(
            "Result of the automated check: 'SUCCESS' when the submission is correct; " +
                    "'MISTAKE' when at least one error was detected. This field MUST be either SUCCESS or MISTAKE."
    )
    private CheckStatus status;

    @JsonPropertyDescription(
            "Model confidence in the check result, from 0.0 (no confidence) to 1.0 (highest confidence)."
    )
    private Float trustLevel;

    @JsonPropertyDescription(
            "Type of the detected error, or 'NONE' when no error is present. Must be 'NONE' when status is 'SUCCESS'."
    )
    private CheckErrorType errorType;

    @JsonPropertyDescription(
            "Human-readable explanation: for a mistake, describes what is wrong and (if possible) where;" +
                    "for a correct solution, a short confirmation or brief rationale."
    )
    private String explanation;

    public CheckResult toCheckResult(String submissionId) {
        return CheckResult.builder()
                .submissionId(submissionId)
                .status(status)
                .trustLevel(trustLevel)
                .errorType(errorType)
                .explanation(explanation)
                .build();
    }
}
