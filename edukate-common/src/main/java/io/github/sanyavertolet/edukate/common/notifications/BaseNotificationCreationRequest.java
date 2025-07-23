package io.github.sanyavertolet.edukate.common.notifications;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.Objects;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "_type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleNotificationCreationRequest.class, name = "simple"),
        @JsonSubTypes.Type(value = InviteNotificationCreationRequest.class, name = "invite")
})
@Getter
public sealed class BaseNotificationCreationRequest permits
        SimpleNotificationCreationRequest,
        InviteNotificationCreationRequest {
    private final String uuid;
    private final String targetUserId;

    @PersistenceCreator
    public BaseNotificationCreationRequest(String uuid, String targetUserId) {
        Objects.requireNonNull(uuid, "UUID must not be null");
        Objects.requireNonNull(targetUserId, "userId must not be null");
        this.uuid = uuid;
        this.targetUserId = targetUserId;
    }
}
