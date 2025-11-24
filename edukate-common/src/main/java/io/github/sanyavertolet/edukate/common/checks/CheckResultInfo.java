package io.github.sanyavertolet.edukate.common.checks;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CheckResultInfo {
    private String id;
    private CheckStatus status;
    private Float trustLevel;
    private Instant createdAt;
}
