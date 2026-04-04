export interface ProblemMetadata {
    name: string;
    isHard: boolean;
    tags: string[];
    status?: ProblemStatus;
}

export type ProblemStatus = "SOLVED" | "SOLVING" | "FAILED" | "NOT_SOLVED";

export interface Problem {
    id: string;
    isHard: boolean;
    hasResult: boolean;
    tags: string[];
    text?: string;
    images: string[];
    status?: ProblemStatus;
    subtasks: Subtask[];
}

export interface Subtask {
    id: string;
    text: string;
}

export interface Result {
    id: string;
    text: string;
    notes: string | null;
    type: ResultType;
    images: string[];
}

export type ResultType = "FORMULA" | "TEXT" | "NUMERIC";
