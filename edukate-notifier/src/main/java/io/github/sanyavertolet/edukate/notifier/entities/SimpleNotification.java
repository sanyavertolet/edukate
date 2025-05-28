package io.github.sanyavertolet.edukate.notifier.entities;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.notifier.dtos.SimpleNotificationDto;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@TypeAlias("simple")
@JsonTypeName("simple")
public final class SimpleNotification extends BaseNotification {
    private final String title;
    private final String message;
    private final String source;

    @PersistenceCreator
    public SimpleNotification(ObjectId _id, String uuid, String userId, LocalDateTime createdAt, String title, String message, String source) {
        this(_id, uuid, false, userId, createdAt, title, message, source);
    }

    public SimpleNotification(ObjectId _id, String uuid, Boolean isRead, String userId, LocalDateTime createdAt, String title, String message, String source) {
        super(_id, uuid, isRead, userId, createdAt != null ? createdAt : LocalDateTime.now());
        this.title = title;
        this.message = message;
        this.source = source;
    }

    public SimpleNotification(String uuid, String userId, LocalDateTime createdAt, String title, String message, String source) {
        this(null, uuid, userId, createdAt, title, message, source);
    }

    public SimpleNotification(String userId, String title, String message, String source) {
        this(UUID.randomUUID().toString(), userId, LocalDateTime.now(), title, message, source);
    }

    @Override
    public SimpleNotificationDto toDto() {
        return new SimpleNotificationDto(getUuid(), getUserId(), getIsRead(), getCreatedAt(), title, message, source);
    }

    public static SimpleNotification fromDto(SimpleNotificationDto notificationDto) {
        return new SimpleNotification(
                notificationDto.getUuid(),
                notificationDto.getUserId(),
                notificationDto.getCreatedAt() != null ? notificationDto.getCreatedAt() : LocalDateTime.now(),
                notificationDto.getTitle(),
                notificationDto.getMessage(),
                notificationDto.getSource()
        );
    }
}
