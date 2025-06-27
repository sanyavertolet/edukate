package io.github.sanyavertolet.edukate.common.notifications;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
@AllArgsConstructor
public sealed class BaseNotificationCreationRequest permits
        SimpleNotificationCreationRequest,
        InviteNotificationCreationRequest {
    private String uuid;
    private String userId;
}
