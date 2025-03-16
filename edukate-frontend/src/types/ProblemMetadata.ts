export interface ProblemMetadata {
  name: string;
  isHard: boolean;
  tags: string[];
  status: ProblemStatus | null;
}

export type ProblemStatus = "SOLVED" | "SOLVING" | "FAILED" | "NOT_SOLVED";
