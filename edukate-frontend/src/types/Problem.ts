import { ProblemStatus } from "./ProblemMetadata";

export interface Problem {
  id: string;
  isHard: boolean;
  hasResult: boolean;
  tags: string[];
  text: string | null;
  images: string[];
  status: ProblemStatus | null;
  subtasks: Subtask[]
}

export interface Subtask {
  id: string;
  text: string;
}
