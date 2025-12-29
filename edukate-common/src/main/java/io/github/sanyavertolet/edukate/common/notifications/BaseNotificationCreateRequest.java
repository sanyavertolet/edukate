package io.github.sanyavertolet.edukate.common.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Objects;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "_type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BaseNotificationCreateRequest.class, name = "base"),
        @JsonSubTypes.Type(value = SimpleNotificationCreateRequest.class, name = "simple"),
        @JsonSubTypes.Type(value = InviteNotificationCreateRequest.class, name = "invite"),
        @JsonSubTypes.Type(value = CheckedNotificationCreateRequest.class, name = "checked"),
})
@Getter
@ToString
@SuperBuilder
public sealed class BaseNotificationCreateRequest permits
        SimpleNotificationCreateRequest,
        InviteNotificationCreateRequest,
        CheckedNotificationCreateRequest {

    @NotBlank
    @Builder.Default
    private final String uuid = UUID.randomUUID().toString();
    @NotBlank
    private final String targetUserId;

    @JsonCreator
    public BaseNotificationCreateRequest(String uuid, String targetUserId) {
        this.uuid = Objects.requireNonNull(uuid, "UUID must not be null");
        this.targetUserId = Objects.requireNonNull(targetUserId, "userId must not be null");
    }
}
