package io.github.sanyavertolet.edukate.notifier.entities;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest;
import io.github.sanyavertolet.edukate.notifier.dtos.SimpleNotificationDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;

@Getter
@Setter
@NoArgsConstructor
@TypeAlias("simple")
@JsonTypeName("simple")
public final class SimpleNotification extends BaseNotification {
    private String title;
    private String message;
    private String source;

    public SimpleNotification(String uuid, String targetUserId, String title, String message, String source) {
        super(uuid, targetUserId);
        this.title = title;
        this.message = message;
        this.source = source;
    }

    @Override
    public SimpleNotificationDto toDto() {
        return new SimpleNotificationDto(getUuid(), getIsRead(), getCreatedAt(), title, message, source);
    }

    public static SimpleNotification fromCreationRequest(SimpleNotificationCreateRequest creationRequest) {
        return new SimpleNotification(
                creationRequest.getUuid(), creationRequest.getTargetUserId(),
                creationRequest.getTitle(), creationRequest.getMessage(), creationRequest.getSource()
        );
    }
}
