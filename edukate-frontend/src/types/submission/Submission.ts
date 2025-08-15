export interface Submission {
    problemId: string;
    userName: string;
    status: SubmissionStatus;
    createdAt: string;
    fileUrls: string[];
}

export type SubmissionStatus = "PENDING" | "SUCCESS" | "FAILED";
