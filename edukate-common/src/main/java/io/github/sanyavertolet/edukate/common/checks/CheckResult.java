package io.github.sanyavertolet.edukate.common.checks;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "check_results")
public class CheckResult {
    private String id;
    private String submissionId;
    private CheckStatus status;
    private Float trustLevel;
    private CheckErrorType errorType;
    private String explanation;

    public static CheckResultBuilder self() {
        return CheckResult.builder()
                .status(CheckStatus.SUCCESS)
                .errorType(CheckErrorType.NONE)
                .explanation("User considered this problem as solved.")
                .id(null)
                .trustLevel(0.01f);
    }
}
