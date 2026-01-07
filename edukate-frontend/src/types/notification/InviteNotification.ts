import { BaseNotification } from "./BaseNotification";

export type InviteNotification = BaseNotification & {
    inviterName: string,
    bundleName: string,
    bundleShareCode: string,
}
