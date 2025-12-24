import { UtcIsoString } from "../common/DateTypes";

export type Submission = {
    id: string;
    problemId: string;
    userName: string;
    status: SubmissionStatus;
    createdAt: UtcIsoString;
    fileUrls: string[];
}

export type SubmissionStatus = "PENDING" | "SUCCESS" | "FAILED";
