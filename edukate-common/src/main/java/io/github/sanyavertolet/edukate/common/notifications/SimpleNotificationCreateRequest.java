package io.github.sanyavertolet.edukate.common.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
@JsonTypeName("simple")
public final class SimpleNotificationCreateRequest extends BaseNotificationCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String message;
    @NotBlank
    private String source;

    @JsonCreator
    public SimpleNotificationCreateRequest(
            String uuid, String targetUserId, String title, String message, String source
    ) {
        super(uuid, targetUserId);
        this.title = title;
        this.message = message;
        this.source = source;
    }
}
