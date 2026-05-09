import type {
    ProblemMetadata as GeneratedProblemMetadata,
    ProblemDto as GeneratedProblemDto,
    AnswerDto as GeneratedAnswerDto,
    Subproblem as GeneratedSubproblem,
} from "@/generated/backend";

export type { ProblemMetadataStatus, ProblemDtoStatus as ProblemStatus } from "@/generated/backend";

export type Subproblem = GeneratedSubproblem;
export type ProblemMetadata = GeneratedProblemMetadata;
export type Problem = GeneratedProblemDto;
export type Answer = GeneratedAnswerDto;
