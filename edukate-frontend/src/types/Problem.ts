export interface Problem {
  id: string;
  isHard: boolean;
  hasResult: boolean;
  tags: string[];
  text: string | null;
  images: string[];
  subtasks: Subtask[]
}

export interface Subtask {
  id: string;
  text: string;
}
