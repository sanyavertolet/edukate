export type {
    CheckResultInfo,
    CheckResultInfoStatus as CheckStatus,
    CheckResultInfoErrorType,
    CheckResultDto,
    CheckResultDtoErrorType,
} from "@/generated/backend";

export type CheckType = "self" | "ai" | "supervisor";

export type CheckRequest = {
    submissionId: string;
    checkType: CheckType;
    problemKey: string;
};
