package io.github.sanyavertolet.edukate.notifier.dtos;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.common.checks.CheckStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@JsonTypeName("checked")
public final class CheckedNotificationDto extends BaseNotificationDto {
    private final String submissionId;
    private final String problemId;
    private final CheckStatus status;

    public CheckedNotificationDto(
            String uuid, Boolean isRead, Instant createdAt, String submissionId, String problemId, CheckStatus status
    ) {
        super(uuid, isRead, createdAt);
        this.submissionId = submissionId;
        this.problemId = problemId;
        this.status = status;
    }
}
