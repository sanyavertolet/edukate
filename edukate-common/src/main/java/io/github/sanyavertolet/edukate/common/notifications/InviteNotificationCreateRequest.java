package io.github.sanyavertolet.edukate.common.notifications;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("invite")
public final class InviteNotificationCreateRequest extends BaseNotificationCreateRequest {
    @NotBlank
    private String inviterName;
    @NotBlank
    private String bundleName;
    @NotBlank
    private String bundleShareCode;

    public InviteNotificationCreateRequest(
            String uuid, String targetUserId, String inviterName, String bundleName, String bundleShareCode
    ) {
        super(uuid, targetUserId);
        this.inviterName = inviterName;
        this.bundleName = bundleName;
        this.bundleShareCode = bundleShareCode;
    }

    public static InviteNotificationCreateRequest of(
            String uuid, String targetUserId, String inviter, String bundleName, String bundleShareCode
    ) {
        return new InviteNotificationCreateRequest(uuid, targetUserId, inviter, bundleName, bundleShareCode);
    }
}
