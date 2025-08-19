package io.github.sanyavertolet.edukate.notifier.entities;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest;
import io.github.sanyavertolet.edukate.notifier.dtos.InviteNotificationDto;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;

import java.time.LocalDateTime;

@Getter
@Setter
@TypeAlias("invite")
@JsonTypeName("invite")
public final class InviteNotification extends BaseNotification {
    private final String inviterName;
    private final String bundleName;
    private final String bundleShareCode;

    @PersistenceCreator
    public InviteNotification(
            ObjectId _id, String uuid, String targetUserId, LocalDateTime createdAt,
            String inviterName, String bundleName, String bundleShareCode
    ) {
        this(_id, uuid, false, targetUserId, createdAt, inviterName, bundleName, bundleShareCode);
    }

    public InviteNotification(
            ObjectId _id, String uuid, Boolean isRead, String targetUserId,
            LocalDateTime createdAt, String inviterName, String bundleName, String bundleShareCode
    ) {
        super(_id, uuid, isRead, targetUserId, createdAt != null ? createdAt : LocalDateTime.now());
        this.inviterName = inviterName;
        this.bundleName = bundleName;
        this.bundleShareCode = bundleShareCode;
    }

    public InviteNotification(
            String uuid, String targetUserId, String inviterName, String bundleName, String bundleShareCode
    ) {
        this(null, uuid, targetUserId, LocalDateTime.now(), inviterName, bundleName, bundleShareCode);
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
