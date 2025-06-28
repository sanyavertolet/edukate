import { BaseNotification } from "./BaseNotification";

export interface SimpleNotification extends BaseNotification {
    title: string;
    message: string;
    source: string;
}
