export interface Submission {
    problemId: string;
    userId: string;
    status: SubmissionStatus;
    createdAt: Date;
}

export type SubmissionStatus = "PENDING" | "SUCCESS" | "ERROR";
