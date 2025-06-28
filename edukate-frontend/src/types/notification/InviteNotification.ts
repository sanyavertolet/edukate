import { BaseNotification } from "./BaseNotification";

export interface InviteNotification extends BaseNotification {
    inviter: string;
    bundleName: string;
    bundleShareCode: string;
}
