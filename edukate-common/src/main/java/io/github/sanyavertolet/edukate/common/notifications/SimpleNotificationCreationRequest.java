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

    public SimpleNotificationCreationRequest(String uuid, String userId, String title, String message, String source) {
        super(uuid, userId);
        this.title = title;
        this.message = message;
        this.source = source;
    }

    public static SimpleNotificationCreationRequest of(
            String uuid, String userId, String title, String message, String source
    ) {
        return new SimpleNotificationCreationRequest(uuid, userId, title, message, source);
    }
}
