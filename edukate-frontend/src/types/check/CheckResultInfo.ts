import { CheckStatus } from "./CheckResult";
import { UtcIsoString } from "../common/DateTypes";

export type CheckResultInfo = {
    id: string;
    status: CheckStatus;
    trustLevel: number;
    createdAt: UtcIsoString;
}
