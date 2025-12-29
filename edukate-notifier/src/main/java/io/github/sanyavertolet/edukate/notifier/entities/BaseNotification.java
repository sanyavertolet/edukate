package io.github.sanyavertolet.edukate.notifier.entities;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest;
import io.github.sanyavertolet.edukate.common.notifications.CheckedNotificationCreateRequest;
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest;
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest;
import io.github.sanyavertolet.edukate.notifier.dtos.BaseNotificationDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "notifications")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "_type",
        defaultImpl = BaseNotification.class,
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BaseNotification.class, name = "base"),
        @JsonSubTypes.Type(value = SimpleNotification.class, name = "simple"),
        @JsonSubTypes.Type(value = InviteNotification.class, name = "invite"),
        @JsonSubTypes.Type(value = CheckedNotification.class, name = "checked"),
})
@JsonTypeName("base")
@NoArgsConstructor
public sealed class BaseNotification permits
        SimpleNotification,
        InviteNotification,
        CheckedNotification {
    @Id
    private String _id;

    @Indexed(unique = true)
    private String uuid;

    @NonNull
    private Boolean isRead;

    private String targetUserId;

    @CreatedDate
    private Instant createdAt;

    public BaseNotification(String uuid, String targetUserId) {
        this.uuid = uuid;
        this.targetUserId = targetUserId;
        this.isRead = false;
    }

    public static BaseNotification fromCreationRequest(BaseNotificationCreateRequest request) {
        return switch (request) {
            case SimpleNotificationCreateRequest req -> SimpleNotification.fromCreationRequest(req);
            case InviteNotificationCreateRequest req -> InviteNotification.fromCreationRequest(req);
            case CheckedNotificationCreateRequest req -> CheckedNotification.fromCreationRequest(req);
            default -> throw new UnsupportedOperationException("Unsupported DTO type: " + request.getClass().getName());
        };
    }

    public BaseNotificationDto toDto() {
        return switch (this) {
            case SimpleNotification simpleNotification -> simpleNotification.toDto();
            case InviteNotification inviteNotification -> inviteNotification.toDto();
            case CheckedNotification checkedNotification -> checkedNotification.toDto();
            default ->
                    throw new UnsupportedOperationException("Unsupported Notification type: " + getClass().getName());
        };
    }
}
