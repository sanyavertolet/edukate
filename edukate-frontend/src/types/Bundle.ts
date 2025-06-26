import { ProblemMetadata } from "./ProblemMetadata";

export interface Bundle {
    name: string;
    description: string;
    admins: string[];
    isPublic: boolean;
    problems: ProblemMetadata[];
    shareCode: string;
}

export type BundleCategory = "owned" | "public" | "joined";
