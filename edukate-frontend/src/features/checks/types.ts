export type { CheckResultInfo, CheckResultInfoStatus as CheckStatus } from "@/generated/backend";

export type CheckType = "self" | "ai" | "supervisor";

export type CheckRequest = {
    submissionId: string;
    checkType: CheckType;
};
