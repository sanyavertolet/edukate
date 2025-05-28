package io.github.sanyavertolet.edukate.notifier.dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "_type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleNotificationDto.class, name = "simple")
})
@Getter
@AllArgsConstructor
public sealed class BaseNotificationDto permits SimpleNotificationDto {
    private String uuid;
    private String userId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
