import { VERDICT_DRAFT_TTL_MS, VerdictDraft } from "./types";

const draftKey = (ticketId: number) => `supervisor_verdict_draft_${String(ticketId)}`;
const DRAFT_KEY_PATTERN = /^supervisor_verdict_draft_(\d+)$/;

export function loadDraft(ticketId: number): VerdictDraft | null {
    try {
        const raw = localStorage.getItem(draftKey(ticketId));
        if (!raw) return null;
        return JSON.parse(raw) as VerdictDraft;
    } catch {
        return null;
    }
}

export function saveDraft(ticketId: number, draft: VerdictDraft): void {
    localStorage.setItem(draftKey(ticketId), JSON.stringify(draft));
}

export function clearDraft(ticketId: number): void {
    localStorage.removeItem(draftKey(ticketId));
}

export function pruneOldDrafts(): void {
    const cutoff = Date.now() - VERDICT_DRAFT_TTL_MS;
    for (let i = localStorage.length - 1; i >= 0; i--) {
        const key = localStorage.key(i);
        if (!key || !DRAFT_KEY_PATTERN.test(key)) continue;
        try {
            const raw = localStorage.getItem(key);
            if (!raw) continue;
            const draft = JSON.parse(raw) as VerdictDraft;
            if (draft.savedAt < cutoff) {
                localStorage.removeItem(key);
            }
        } catch {
            localStorage.removeItem(key);
        }
    }
}
