package io.github.sanyavertolet.edukate.notifier.entities;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreationRequest;
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreationRequest;
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreationRequest;
import io.github.sanyavertolet.edukate.notifier.dtos.BaseNotificationDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

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
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
public sealed class BaseNotification permits SimpleNotification, InviteNotification {
    @Id
    private ObjectId _id;

    @Indexed(unique = true)
    private String uuid;

    private Boolean isRead;

    private String targetUserId;

    private LocalDateTime createdAt;

    public static BaseNotification fromCreationRequest(BaseNotificationCreationRequest creationRequest) {
        if (creationRequest instanceof SimpleNotificationCreationRequest simpleNotificationCreationRequest) {
            return SimpleNotification.fromCreationRequest(simpleNotificationCreationRequest);
        } else if (creationRequest instanceof InviteNotificationCreationRequest inviteNotificationCreationRequest) {
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
