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
@JsonTypeName("invite")
public final class InviteNotificationCreateRequest extends BaseNotificationCreateRequest {
    @NotBlank
    private String inviterName;
    @NotBlank
    private String bundleName;
    @NotBlank
    private String bundleShareCode;

    @JsonCreator
    public InviteNotificationCreateRequest(
            String uuid, String targetUserId, String inviterName, String bundleName, String bundleShareCode
    ) {
        super(uuid, targetUserId);
        this.inviterName = inviterName;
        this.bundleName = bundleName;
        this.bundleShareCode = bundleShareCode;
    }
}
