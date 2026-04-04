import { UtcIsoString } from "@/shared/utils/date-types";

export type SubmissionStatus = "PENDING" | "SUCCESS" | "FAILED";

export type Submission = {
    id: string;
    problemId: string;
    userName: string;
    status: SubmissionStatus;
    createdAt: UtcIsoString;
    fileUrls: string[];
};

export type CreateSubmissionRequest = {
    problemId: string;
    fileNames: string[];
};
