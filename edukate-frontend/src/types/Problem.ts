import { ProblemStatus } from "./ProblemMetadata";

export interface Problem {
  id: string;
  isHard: boolean;
  hasResult: boolean;
  tags: string[];
  text?: string;
  images: string[];
  status?: ProblemStatus;
  subtasks: Subtask[]
}

export interface Subtask {
  id: string;
  text: string;
}
