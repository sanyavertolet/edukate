package io.github.sanyavertolet.edukate.notifier.entities;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.common.checks.CheckStatus;
import io.github.sanyavertolet.edukate.common.notifications.CheckedNotificationCreateRequest;
import io.github.sanyavertolet.edukate.notifier.dtos.CheckedNotificationDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeName("checked")
public final class CheckedNotification extends BaseNotification {
    private String submissionId;
    private String problemId;
    private CheckStatus status;

    public CheckedNotification(
            String uuid, String targetUserId, String submissionId, String problemId, CheckStatus status
    ) {
        super(uuid, targetUserId);
        this.submissionId = submissionId;
        this.problemId = problemId;
        this.status = status;
    }

    @Override
    public CheckedNotificationDto toDto() {
        return new CheckedNotificationDto(getUuid(), getIsRead(), getCreatedAt(), submissionId, problemId, status);
    }

    public static CheckedNotification fromCreationRequest(CheckedNotificationCreateRequest creationRequest) {
        return new CheckedNotification(
                creationRequest.getUuid(), creationRequest.getTargetUserId(),
                creationRequest.getSubmissionId(), creationRequest.getProblemId(), creationRequest.getStatus()
        );
    }
}
