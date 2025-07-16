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
            String uuid, String targetUserName, String inviterName, String bundleName, String bundleShareCode
    ) {
        super(uuid, targetUserName);
        this.inviterName = inviterName;
        this.bundleName = bundleName;
        this.bundleShareCode = bundleShareCode;
    }

    public static InviteNotificationCreationRequest of(
            String uuid, String targetUserName, String inviter, String bundleName, String bundleShareCode
    ) {
        return new InviteNotificationCreationRequest(uuid, targetUserName, inviter, bundleName, bundleShareCode);
    }
}
