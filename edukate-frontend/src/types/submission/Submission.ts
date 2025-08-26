import { UtcIsoString } from "../common/DateTypes";

export interface Submission {
    problemId: string;
    userName: string;
    status: SubmissionStatus;
    createdAt: UtcIsoString;
    fileUrls: string[];
}

export type SubmissionStatus = "PENDING" | "SUCCESS" | "FAILED";
