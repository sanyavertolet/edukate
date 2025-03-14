export interface Problem {
  id: string;
  isHard: boolean;
  hasResult: boolean;
  tags: string[];
  text: string | null;
  images: string[];
  subtasks: ({id: string, text: string})[]
}
