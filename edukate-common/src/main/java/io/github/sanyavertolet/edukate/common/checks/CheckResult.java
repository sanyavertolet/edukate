package io.github.sanyavertolet.edukate.common.checks;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "check_results")
public class CheckResult {
    private String submissionId;
    private CheckStatus status;
    private Float trustLevel;
    private CheckErrorType errorType;
    private String explanation;
}
