package io.github.sanyavertolet.edukate.common.notifications;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("invite")
public final class InviteNotificationCreationRequest extends BaseNotificationCreationRequest {
    private String inviter;
    private String bundleName;
    private String bundleShareCode;

    public InviteNotificationCreationRequest(
            String uuid, String userId, String inviter, String bundleName, String bundleShareCode
    ) {
        super(uuid, userId);
        this.inviter = inviter;
        this.bundleName = bundleName;
        this.bundleShareCode = bundleShareCode;
    }

    public static InviteNotificationCreationRequest of(
            String uuid, String userId, String inviter, String bundleName, String bundleShareCode
    ) {
        return new InviteNotificationCreationRequest(uuid, userId, inviter, bundleName, bundleShareCode);
    }
}
