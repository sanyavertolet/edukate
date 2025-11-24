package io.github.sanyavertolet.edukate.backend.dtos;

import io.github.sanyavertolet.edukate.common.checks.CheckErrorType;
import io.github.sanyavertolet.edukate.common.checks.CheckStatus;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Data
@Builder
@Jacksonized
public class CheckResultDto {
    private CheckStatus status;
    private Float trustLevel;
    private CheckErrorType errorType;
    private String explanation;
    private Instant createdAt;
}
