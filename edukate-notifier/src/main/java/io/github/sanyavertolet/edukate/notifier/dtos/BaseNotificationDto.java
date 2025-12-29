package io.github.sanyavertolet.edukate.notifier.dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "_type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BaseNotificationDto.class, name = "base"),
        @JsonSubTypes.Type(value = SimpleNotificationDto.class, name = "simple"),
        @JsonSubTypes.Type(value = InviteNotificationDto.class, name = "invite"),
        @JsonSubTypes.Type(value = CheckedNotificationDto.class, name = "checked"),
})
@Getter
@AllArgsConstructor
public sealed class BaseNotificationDto permits
        SimpleNotificationDto,
        InviteNotificationDto,
        CheckedNotificationDto {
    private String uuid;
    private Boolean isRead;
    private Instant createdAt;
}
