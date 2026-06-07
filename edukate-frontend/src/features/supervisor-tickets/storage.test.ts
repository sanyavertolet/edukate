import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { clearDraft, loadDraft, pruneOldDrafts, saveDraft } from "./storage";
import { VERDICT_DRAFT_TTL_MS, VerdictDraft } from "./types";

const TICKET_ID = 42;
const DRAFT_KEY = `supervisor_verdict_draft_${String(TICKET_ID)}`;

const sampleDraft: VerdictDraft = {
    status: "MISTAKE",
    errorType: "ALGEBRAIC",
    explanation: "Wrong sign",
    savedAt: Date.now(),
};

beforeEach(() => {
    localStorage.clear();
});

afterEach(() => {
    vi.useRealTimers();
});

describe("loadDraft", () => {
    it("returns null when nothing stored", () => {
        expect(loadDraft(TICKET_ID)).toBeNull();
    });

    it("returns parsed draft when key exists", () => {
        localStorage.setItem(DRAFT_KEY, JSON.stringify(sampleDraft));
        expect(loadDraft(TICKET_ID)).toEqual(sampleDraft);
    });

    it("returns null when stored JSON is malformed", () => {
        localStorage.setItem(DRAFT_KEY, "{not valid json}}");
        expect(loadDraft(TICKET_ID)).toBeNull();
    });
});

describe("saveDraft", () => {
    it("persists draft with savedAt timestamp", () => {
        saveDraft(TICKET_ID, sampleDraft);
        const stored = JSON.parse(localStorage.getItem(DRAFT_KEY) ?? "null") as VerdictDraft;
        expect(stored).toMatchObject({ status: "MISTAKE", explanation: "Wrong sign" });
    });
});

describe("clearDraft", () => {
    it("removes key from localStorage", () => {
        localStorage.setItem(DRAFT_KEY, JSON.stringify(sampleDraft));
        clearDraft(TICKET_ID);
        expect(localStorage.getItem(DRAFT_KEY)).toBeNull();
    });
});

describe("pruneOldDrafts", () => {
    it("removes drafts older than TTL", () => {
        const oldDraft: VerdictDraft = { ...sampleDraft, savedAt: Date.now() - VERDICT_DRAFT_TTL_MS - 1 };
        localStorage.setItem(DRAFT_KEY, JSON.stringify(oldDraft));
        pruneOldDrafts();
        expect(localStorage.getItem(DRAFT_KEY)).toBeNull();
    });

    it("preserves recent drafts", () => {
        const recentDraft: VerdictDraft = { ...sampleDraft, savedAt: Date.now() };
        localStorage.setItem(DRAFT_KEY, JSON.stringify(recentDraft));
        pruneOldDrafts();
        expect(localStorage.getItem(DRAFT_KEY)).not.toBeNull();
    });

    it("ignores unrelated localStorage keys", () => {
        localStorage.setItem("some_other_key", "value");
        pruneOldDrafts();
        expect(localStorage.getItem("some_other_key")).toBe("value");
    });
});
