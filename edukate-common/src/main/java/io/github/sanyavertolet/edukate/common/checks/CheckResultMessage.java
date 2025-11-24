package io.github.sanyavertolet.edukate.common.checks;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CheckResultMessage {
    private String submissionId;
    private CheckStatus status;
    private Float trustLevel;
    private CheckErrorType errorType;
    private String explanation;
}
