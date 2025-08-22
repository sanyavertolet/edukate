package io.github.sanyavertolet.edukate.notifier.entities;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest;
import io.github.sanyavertolet.edukate.notifier.dtos.SimpleNotificationDto;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;

import java.time.LocalDateTime;

@Getter
@Setter
@TypeAlias("simple")
@JsonTypeName("simple")
public final class SimpleNotification extends BaseNotification {
    private final String title;
    private final String message;
    private final String source;

    @PersistenceCreator
    public SimpleNotification(
            ObjectId _id, String uuid, String targetUserId, LocalDateTime createdAt, String title, String message,
            String source
    ) {
        this(_id, uuid, false, targetUserId, createdAt, title, message, source);
    }

    public SimpleNotification(
            ObjectId _id, String uuid, Boolean isRead, String targetUserId,
            LocalDateTime createdAt, String title, String message, String source
    ) {
        super(_id, uuid, isRead, targetUserId, createdAt != null ? createdAt : LocalDateTime.now());
        this.title = title;
        this.message = message;
        this.source = source;
    }

    public SimpleNotification(String uuid, String targetUserId, String title, String message, String source) {
        this(null, uuid, targetUserId, LocalDateTime.now(), title, message, source);
    }

    @Override
    public SimpleNotificationDto toDto() {
        return new SimpleNotificationDto(getUuid(), getIsRead(), getCreatedAt(), title, message, source);
    }

    public static SimpleNotification fromCreationRequest(SimpleNotificationCreateRequest creationRequest) {
        return new SimpleNotification(
                creationRequest.getUuid(), creationRequest.getTargetUserId(),
                creationRequest.getTitle(), creationRequest.getMessage(), creationRequest.getSource()
        );
    }
}
