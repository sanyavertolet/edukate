package io.github.sanyavertolet.edukate.common.checks;

import lombok.Data;

@Data
public class ModelResponse {
    private CheckStatus status;
    private Float trustLevel;
    private CheckErrorType errorType;
    private String explanation;

    public CheckResult toCheckResult(String submissionId) {
        return new CheckResult(submissionId, status, trustLevel, errorType, explanation);
    }
}
