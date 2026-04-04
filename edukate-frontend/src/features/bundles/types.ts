import { ProblemMetadata } from "@/features/problems/types";

export interface Bundle {
    name: string;
    description: string;
    admins: string[];
    isPublic: boolean;
    problems: ProblemMetadata[];
    shareCode: string;
}

export type BundleCategory = "owned" | "public" | "joined";

export interface BundleMetadata {
    name: string;
    description: string;
    admins: string[];
    isPublic: boolean;
    shareCode: string;
    size: number;
}

export interface CreateBundleRequest {
    name: string;
    description: string;
    isPublic: boolean;
    problemIds: string[];
}
