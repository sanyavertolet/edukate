import { BaseNotification } from "./BaseNotification";
import { CheckStatus } from "../check/CheckResult";

export type CheckedNotification = BaseNotification & {
    submissionId: string,
    problemId: string,
    status: CheckStatus,
}
