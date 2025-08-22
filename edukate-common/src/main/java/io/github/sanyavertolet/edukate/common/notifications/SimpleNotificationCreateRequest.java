package io.github.sanyavertolet.edukate.common.notifications;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("simple")
public final class SimpleNotificationCreateRequest extends BaseNotificationCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String message;
    @NotBlank
    private String source;

    public SimpleNotificationCreateRequest(
            String uuid, String targetUserId, String title, String message, String source
    ) {
        super(uuid, targetUserId);
        this.title = title;
        this.message = message;
        this.source = source;
    }

    public static SimpleNotificationCreateRequest of(
            String uuid, String targetUserId, String title, String message, String source
    ) {
        return new SimpleNotificationCreateRequest(uuid, targetUserId, title, message, source);
    }
}
