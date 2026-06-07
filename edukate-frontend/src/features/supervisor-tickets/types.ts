export type { SupervisorTicketDto, SupervisorTicketDtoStatus as SupervisorTicketStatus } from "@/generated/backend";

export type VerdictDraft = {
    status: "SUCCESS" | "MISTAKE" | null;
    errorType: string | null;
    explanation: string;
    savedAt: number;
};

export const VERDICT_DRAFT_TTL_MS = 7 * 24 * 60 * 60 * 1000;
