package io.github.sanyavertolet.edukate.notifier.entities;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreationRequest;
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
    private final String inviter;
    private final String bundleName;
    private final String bundleShareCode;

    @PersistenceCreator
    public InviteNotification(
            ObjectId _id, String uuid, String userId, LocalDateTime createdAt,
            String inviter, String bundleName, String bundleShareCode
    ) {
        this(_id, uuid, false, userId, createdAt, inviter, bundleName, bundleShareCode);
    }

    public InviteNotification(
            ObjectId _id, String uuid, Boolean isRead, String userId,
            LocalDateTime createdAt, String inviter, String bundleName, String bundleShareCode
    ) {
        super(_id, uuid, isRead, userId, createdAt != null ? createdAt : LocalDateTime.now());
        this.inviter = inviter;
        this.bundleName = bundleName;
        this.bundleShareCode = bundleShareCode;
    }

    public InviteNotification(
            String uuid, String userId, String inviter, String bundleName, String bundleShareCode
    ) {
        this(null, uuid, userId, LocalDateTime.now(), inviter, bundleName, bundleShareCode);
    }

    @Override
    public InviteNotificationDto toDto() {
        return new InviteNotificationDto(
                getUuid(), getUserId(), getIsRead(), getCreatedAt(), inviter, bundleName, bundleShareCode
        );
    }

    public static InviteNotification fromCreationRequest(InviteNotificationCreationRequest creationRequest) {
        return new InviteNotification(
                creationRequest.getUuid(), creationRequest.getUserId(),
                creationRequest.getInviter(), creationRequest.getBundleName(), creationRequest.getBundleShareCode()
        );
    }
}
