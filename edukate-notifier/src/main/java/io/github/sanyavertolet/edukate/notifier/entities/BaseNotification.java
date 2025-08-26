package io.github.sanyavertolet.edukate.notifier.entities;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest;
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest;
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest;
import io.github.sanyavertolet.edukate.notifier.dtos.BaseNotificationDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
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
        @JsonSubTypes.Type(value = SimpleNotification.class, name = "simple"),
        @JsonSubTypes.Type(value = InviteNotification.class, name = "invite")
})
@JsonTypeName("base")
@TypeAlias("base")
@NoArgsConstructor
public sealed class BaseNotification permits SimpleNotification, InviteNotification {
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

    public static BaseNotification fromCreationRequest(BaseNotificationCreateRequest creationRequest) {
        if (creationRequest instanceof SimpleNotificationCreateRequest simpleNotificationCreationRequest) {
            return SimpleNotification.fromCreationRequest(simpleNotificationCreationRequest);
        } else if (creationRequest instanceof InviteNotificationCreateRequest inviteNotificationCreationRequest) {
            return InviteNotification.fromCreationRequest(inviteNotificationCreationRequest);
        }
        throw new UnsupportedOperationException("Unsupported DTO type: " + creationRequest.getClass().getName());
    }

    public BaseNotificationDto toDto() {
        if (this instanceof SimpleNotification simpleNotification) {
            return simpleNotification.toDto();
        } else if (this instanceof InviteNotification inviteNotification) {
            return inviteNotification.toDto();
        }
        throw new UnsupportedOperationException("Unsupported Notification type: " + getClass().getName());
    }
}
