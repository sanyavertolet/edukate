import { UtcIsoString } from "@/shared/utils/date-types";

export type CheckType = "self" | "ai" | "supervisor";

export type CheckRequest = {
    submissionId: string;
    checkType: CheckType;
};

export type CheckStatus = "SUCCESS" | "MISTAKE" | "INTERNAL_ERROR";

export type CheckResultInfo = {
    id: string;
    status: CheckStatus;
    trustLevel: number;
    createdAt: UtcIsoString;
};
