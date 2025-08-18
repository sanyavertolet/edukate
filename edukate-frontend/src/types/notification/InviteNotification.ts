import { BaseNotification } from "./BaseNotification";

export interface InviteNotification extends BaseNotification {
    inviterName: string;
    bundleName: string;
    bundleShareCode: string;
}
