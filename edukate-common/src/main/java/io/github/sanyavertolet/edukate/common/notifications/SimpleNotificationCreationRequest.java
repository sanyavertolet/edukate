package io.github.sanyavertolet.edukate.common.notifications;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("simple")
public final class SimpleNotificationCreationRequest extends BaseNotificationCreationRequest {
    private String title;
    private String message;
    private String source;

    public SimpleNotificationCreationRequest(
            String uuid, String targetUserName, String title, String message, String source
    ) {
        super(uuid, targetUserName);
        this.title = title;
        this.message = message;
        this.source = source;
    }

    public static SimpleNotificationCreationRequest of(
            String uuid, String targetUserId, String title, String message, String source
    ) {
        return new SimpleNotificationCreationRequest(uuid, targetUserId, title, message, source);
    }
}
