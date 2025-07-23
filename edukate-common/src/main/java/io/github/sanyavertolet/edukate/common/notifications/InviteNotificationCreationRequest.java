package io.github.sanyavertolet.edukate.common.notifications;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("invite")
public final class InviteNotificationCreationRequest extends BaseNotificationCreationRequest {
    private String inviterName;
    private String bundleName;
    private String bundleShareCode;

    public InviteNotificationCreationRequest(
            String uuid, String targetUserId, String inviterName, String bundleName, String bundleShareCode
    ) {
        super(uuid, targetUserId);
        this.inviterName = inviterName;
        this.bundleName = bundleName;
        this.bundleShareCode = bundleShareCode;
    }

    public static InviteNotificationCreationRequest of(
            String uuid, String targetUserId, String inviter, String bundleName, String bundleShareCode
    ) {
        return new InviteNotificationCreationRequest(uuid, targetUserId, inviter, bundleName, bundleShareCode);
    }
}
