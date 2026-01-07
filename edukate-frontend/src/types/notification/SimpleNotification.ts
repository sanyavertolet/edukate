import { BaseNotification } from "./BaseNotification";

export type SimpleNotification =  BaseNotification & {
    title: string,
    message: string,
    source: string,
}
