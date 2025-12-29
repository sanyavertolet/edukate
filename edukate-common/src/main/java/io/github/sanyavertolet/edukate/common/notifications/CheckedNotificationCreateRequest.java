package io.github.sanyavertolet.edukate.common.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.common.checks.CheckStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
@JsonTypeName("checked")
public final class CheckedNotificationCreateRequest extends BaseNotificationCreateRequest {
    private String submissionId;
    private String problemId;
    private CheckStatus status;

    @JsonCreator
    public CheckedNotificationCreateRequest(
            String uuid, String targetUserId, String submissionId, String problemId, CheckStatus status
    ) {
        super(uuid, targetUserId);
        this.submissionId = submissionId;
        this.problemId = problemId;
        this.status = status;
    }
}
