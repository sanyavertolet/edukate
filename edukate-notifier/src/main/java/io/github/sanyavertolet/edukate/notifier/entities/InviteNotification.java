package io.github.sanyavertolet.edukate.notifier.entities;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest;
import io.github.sanyavertolet.edukate.notifier.dtos.InviteNotificationDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;

@Getter
@Setter
@NoArgsConstructor
@TypeAlias("invite")
@JsonTypeName("invite")
public final class InviteNotification extends BaseNotification {
    private String inviterName;
    private String bundleName;
    private String bundleShareCode;

    public InviteNotification(
            String uuid, String targetUserId, String inviterName, String bundleName, String bundleShareCode
    ) {
        super(uuid, targetUserId);
        this.inviterName = inviterName;
        this.bundleName = bundleName;
        this.bundleShareCode = bundleShareCode;
    }

    @Override
    public InviteNotificationDto toDto() {
        return new InviteNotificationDto(
                getUuid(), getIsRead(), getCreatedAt(), inviterName, bundleName, bundleShareCode
        );
    }

    public static InviteNotification fromCreationRequest(InviteNotificationCreateRequest creationRequest) {
        return new InviteNotification(
                creationRequest.getUuid(), creationRequest.getTargetUserId(), creationRequest.getInviterName(),
                creationRequest.getBundleName(), creationRequest.getBundleShareCode()
        );
    }
}
