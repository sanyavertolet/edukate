export interface ProblemMetadata {
  name: string;
  isHard: boolean;
  tags: string[];
  status?: ProblemStatus;
}

export type ProblemStatus = "SOLVED" | "SOLVING" | "FAILED" | "NOT_SOLVED";
