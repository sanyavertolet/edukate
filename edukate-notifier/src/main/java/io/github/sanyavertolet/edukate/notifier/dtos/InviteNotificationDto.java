package io.github.sanyavertolet.edukate.notifier.dtos;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonTypeName("simple")
public final class InviteNotificationDto extends BaseNotificationDto {
    private String inviter;
    private String bundleName;
    private String bundleShareCode;

    public InviteNotificationDto(
            String uuid, String userId, Boolean isRead, LocalDateTime createdAt,
            String inviter, String bundleName, String bundleShareCode
    ) {
        super(uuid, userId, isRead, createdAt);
        this.inviter = inviter;
        this.bundleName = bundleName;
        this.bundleShareCode = bundleShareCode;
    }
}
