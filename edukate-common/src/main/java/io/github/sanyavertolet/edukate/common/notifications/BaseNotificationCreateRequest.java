package io.github.sanyavertolet.edukate.common.notifications;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
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
        @JsonSubTypes.Type(value = SimpleNotificationCreateRequest.class, name = "simple"),
        @JsonSubTypes.Type(value = InviteNotificationCreateRequest.class, name = "invite")
})
@Getter
public sealed class BaseNotificationCreateRequest permits
        SimpleNotificationCreateRequest,
        InviteNotificationCreateRequest {
    @NotBlank
    private final String uuid;
    @NotBlank
    private final String targetUserId;

    @PersistenceCreator
    public BaseNotificationCreateRequest(String uuid, String targetUserId) {
        this.uuid = Objects.requireNonNull(uuid, "UUID must not be null");
        this.targetUserId = Objects.requireNonNull(targetUserId, "userId must not be null");
    }
}
