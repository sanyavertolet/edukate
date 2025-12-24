export type CheckType = "self" | "ai" | "supervisor";

export type CheckRequest = {
    submissionId: string;
    checkType: CheckType;
};
