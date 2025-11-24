package io.github.sanyavertolet.edukate.backend.entities;

import io.github.sanyavertolet.edukate.backend.dtos.CheckResultDto;
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType;
import io.github.sanyavertolet.edukate.common.checks.CheckResultInfo;
import io.github.sanyavertolet.edukate.common.checks.CheckStatus;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Jacksonized
@Document(collection = "check_results")
public class CheckResult {
    @Id
    private String id;
    @Indexed
    private String submissionId;

    private CheckStatus status;

    private Float trustLevel;

    private CheckErrorType errorType;

    private String explanation;

    @CreatedDate
    private Instant createdAt;

    public static CheckResultBuilder self() {
        return CheckResult.builder()
                .status(CheckStatus.SUCCESS)
                .errorType(CheckErrorType.NONE)
                .explanation("User considered this problem as solved.")
                .id(null)
                .trustLevel(0.01f);
    }

    public CheckResultDto toCheckResultDto() {
        return CheckResultDto.builder()
                .status(status)
                .trustLevel(trustLevel)
                .errorType(errorType)
                .explanation(explanation)
                .createdAt(createdAt)
                .build();
    }

    public CheckResultInfo toCheckResultInfo() {
        return CheckResultInfo.builder().id(id).status(status).trustLevel(trustLevel).createdAt(createdAt).build();
    }
}
