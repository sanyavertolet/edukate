export type {
    CheckResultInfo,
    CheckResultInfoStatus as CheckStatus,
    CheckResultInfoErrorType,
    CheckResultDto,
    CheckResultDtoErrorType,
} from "@/generated/backend";

export type CheckType = "self" | "ai" | "supervisor";

export type CheckRequest =
    | { checkType: "self" | "ai"; submissionId: string; problemKey: string }
    | {
          checkType: "supervisor";
          submissionId: string;
          problemKey: string;
          problemSetCode: string;
          supervisorName: string;
      };
