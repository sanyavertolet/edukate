package io.github.sanyavertolet.edukate.notifier.dtos;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonTypeName("invite")
public final class InviteNotificationDto extends BaseNotificationDto {
    private String inviterName;
    private String bundleName;
    private String bundleShareCode;

    public InviteNotificationDto(
            String uuid, Boolean isRead, LocalDateTime createdAt, String inviterName, String bundleName,
            String bundleShareCode
    ) {
        super(uuid, isRead, createdAt);
        this.inviterName = inviterName;
        this.bundleName = bundleName;
        this.bundleShareCode = bundleShareCode;
    }
}
