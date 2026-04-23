export type {
    CheckResultInfo,
    CheckResultInfoStatus as CheckStatus,
    CheckResultDto,
    CheckResultDtoErrorType,
} from "@/generated/backend";

export type CheckType = "self" | "ai" | "supervisor";

export type CheckRequest = {
    submissionId: string;
    checkType: CheckType;
};
