package io.github.sanyavertolet.edukate.notifier.dtos;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@JsonTypeName("simple")
public final class SimpleNotificationDto extends BaseNotificationDto {
    private String title;
    private String message;
    private String source;

    public SimpleNotificationDto(
            String uuid, Boolean isRead, Instant createdAt,
            String title, String message, String source
    ) {
        super(uuid, isRead, createdAt);
        this.title = title;
        this.message = message;
        this.source = source;
    }
}
