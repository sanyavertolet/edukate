import { ReactNode } from "react";
import { http, HttpResponse } from "msw";
import userEvent from "@testing-library/user-event";
import { render, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getGetProblemListMockHandler } from "@/generated/backend";
import { ProblemSetProblemsEditorDialog } from "./ProblemSetProblemsEditorDialog";
import { ProblemSet } from "@/features/problem-sets/types";
import { ProblemMetadata } from "@/features/problems/types";

// `@dnd-kit`'s keyboard sortable strategy reads element rects to figure out which item
// is "below" / "above" the active one. jsdom doesn't compute layout, so all rects are
// zero and Arrow keys silently no-op. Mocking DndContext lets us synthesize a drag-end
// event directly — testing the reorder contract (what PATCH body fires) instead of the
// irrelevant input-mechanism details.
type DragEndArgs = { active: { id: string }; over: { id: string } | null };
type DndContextProps = { children: ReactNode; onDragEnd?: (event: DragEndArgs) => void };
let capturedOnDragEnd: ((event: DragEndArgs) => void) | null = null;

vi.mock("@dnd-kit/core", async () => {
    const actual = await vi.importActual<typeof import("@dnd-kit/core")>("@dnd-kit/core");
    return {
        ...actual,
        DndContext: ({ children, onDragEnd }: DndContextProps) => {
            capturedOnDragEnd = onDragEnd ?? null;
            return <>{children}</>;
        },
    };
});

beforeEach(() => {
    capturedOnDragEnd = null;
});

function makeProblem(key: string, overrides: Partial<ProblemMetadata> = {}): ProblemMetadata {
    return {
        key,
        code: key.split("/")[1] ?? key,
        bookSlug: key.split("/")[0] ?? "savchenko",
        isHard: false,
        tags: [],
        status: "NOT_SOLVED",
        createdAt: "2026-01-01T00:00:00Z",
        language: "EN" as const,
        ...overrides,
    };
}

const baseProblemSet: ProblemSet = {
    name: "Mechanics",
    description: "Classical mechanics",
    admins: ["alice"],
    moderators: [],
    isPublic: false,
    shareCode: "MECH01",
    problems: [
        makeProblem("savchenko/1.1", { status: "SOLVED", isHard: true }),
        makeProblem("savchenko/1.2"),
        makeProblem("savchenko/1.3", { status: "FAILED" }),
    ],
};

describe("ProblemSetProblemsEditorDialog", () => {
    it("renders current problems in order", () => {
        render(<ProblemSetProblemsEditorDialog problemSet={baseProblemSet} open onClose={() => {}} />);
        const rows = screen.getAllByText(/savchenko\/1\./);
        expect(rows.map((r) => r.textContent)).toEqual(["savchenko/1.1", "savchenko/1.2", "savchenko/1.3"]);
    });

    it("shows the selected count header", () => {
        render(<ProblemSetProblemsEditorDialog problemSet={baseProblemSet} open onClose={() => {}} />);
        expect(screen.getByText(/Selected problems \(3\)/)).toBeInTheDocument();
    });

    it("shows the empty state when there are no problems (cannot remove last one)", () => {
        const empty: ProblemSet = { ...baseProblemSet, problems: [] };
        render(<ProblemSetProblemsEditorDialog problemSet={empty} open onClose={() => {}} />);
        expect(screen.getByText("No problems in this set yet.")).toBeInTheDocument();
    });

    it("fires PATCH with the filtered list when remove is clicked", async () => {
        let capturedBody: unknown = null;
        server.use(
            http.patch("*/api/v1/problem-sets/:shareCode", async ({ request }) => {
                capturedBody = await request.json();
                return HttpResponse.json({ ...baseProblemSet, problems: baseProblemSet.problems.slice(1) });
            }),
        );
        render(<ProblemSetProblemsEditorDialog problemSet={baseProblemSet} open onClose={() => {}} />);

        const removeButtons = screen.getAllByRole("button", { name: "Remove from set" });
        await userEvent.click(removeButtons[0]);

        await waitFor(() => {
            expect(capturedBody).toEqual({ problemKeys: ["savchenko/1.2", "savchenko/1.3"] });
        });
    });

    it("does not fire PATCH when removing the last problem (would leave set empty)", async () => {
        let calls = 0;
        server.use(
            http.patch("*/api/v1/problem-sets/:shareCode", () => {
                calls++;
                return HttpResponse.json(baseProblemSet);
            }),
        );
        const single: ProblemSet = { ...baseProblemSet, problems: [makeProblem("savchenko/only")] };
        render(<ProblemSetProblemsEditorDialog problemSet={single} open onClose={() => {}} />);

        const removeButtons = screen.getAllByRole("button", { name: "Remove from set" });
        await userEvent.click(removeButtons[0]);
        await new Promise((r) => setTimeout(r, 150));
        expect(calls).toBe(0);
    });

    it("fires PATCH with the appended list when a problem is picked", async () => {
        server.use(
            getGetProblemListMockHandler([
                makeProblem("savchenko/2.1"),
                makeProblem("savchenko/2.2"),
                makeProblem("savchenko/1.1"), // already in set, must be filtered out
            ]),
        );
        let capturedBody: unknown = null;
        server.use(
            http.patch("*/api/v1/problem-sets/:shareCode", async ({ request }) => {
                capturedBody = await request.json();
                return HttpResponse.json(baseProblemSet);
            }),
        );
        render(<ProblemSetProblemsEditorDialog problemSet={baseProblemSet} open onClose={() => {}} />);

        // The picker filters out savchenko/1.1 — only 2.x rows should be clickable
        const newRow = await screen.findByText("savchenko/2.2");
        await userEvent.click(newRow);

        await waitFor(() => {
            expect(capturedBody).toEqual({
                problemKeys: ["savchenko/1.1", "savchenko/1.2", "savchenko/1.3", "savchenko/2.2"],
            });
        });
    });

    it("hides problems that are already in the set from the picker", async () => {
        server.use(
            getGetProblemListMockHandler([
                makeProblem("savchenko/1.1"),
                makeProblem("savchenko/1.2"),
                makeProblem("savchenko/9.9"),
            ]),
        );
        render(<ProblemSetProblemsEditorDialog problemSet={baseProblemSet} open onClose={() => {}} />);

        // 1.1 already in the set: still appears once in the sortable list, but NOT a second time in the picker.
        // 9.9 is not in the set: it appears only in the picker.
        await screen.findByText("savchenko/9.9");
        expect(screen.getAllByText("savchenko/1.1")).toHaveLength(1);
        expect(screen.getByText("savchenko/9.9")).toBeInTheDocument();
    });

    it("fires PATCH with the new order when a drag ends (1.1 → end)", async () => {
        const capturedBodies: unknown[] = [];
        server.use(
            http.patch("*/api/v1/problem-sets/:shareCode", async ({ request }) => {
                capturedBodies.push(await request.json());
                return HttpResponse.json(baseProblemSet);
            }),
        );
        render(<ProblemSetProblemsEditorDialog problemSet={baseProblemSet} open onClose={() => {}} />);
        expect(capturedOnDragEnd).toBeTruthy();

        // Move 1.1 onto 1.3 → arrayMove(0, 2) → expected [1.2, 1.3, 1.1]
        capturedOnDragEnd?.({ active: { id: "savchenko/1.1" }, over: { id: "savchenko/1.3" } });

        await waitFor(
            () => {
                expect(capturedBodies).toHaveLength(1);
                expect(capturedBodies[0]).toEqual({
                    problemKeys: ["savchenko/1.2", "savchenko/1.3", "savchenko/1.1"],
                });
            },
            { timeout: 1500 },
        );
    });

    it("fires PATCH with the new order when a drag ends (1.3 → start)", async () => {
        const capturedBodies: unknown[] = [];
        server.use(
            http.patch("*/api/v1/problem-sets/:shareCode", async ({ request }) => {
                capturedBodies.push(await request.json());
                return HttpResponse.json(baseProblemSet);
            }),
        );
        render(<ProblemSetProblemsEditorDialog problemSet={baseProblemSet} open onClose={() => {}} />);

        // Move 1.3 onto 1.1 → arrayMove(2, 0) → expected [1.3, 1.1, 1.2]
        capturedOnDragEnd?.({ active: { id: "savchenko/1.3" }, over: { id: "savchenko/1.1" } });

        await waitFor(
            () => {
                expect(capturedBodies).toHaveLength(1);
                expect(capturedBodies[0]).toEqual({
                    problemKeys: ["savchenko/1.3", "savchenko/1.1", "savchenko/1.2"],
                });
            },
            { timeout: 1500 },
        );
    });

    it("does not fire PATCH when dropped on itself (no-op drag)", async () => {
        let calls = 0;
        server.use(
            http.patch("*/api/v1/problem-sets/:shareCode", () => {
                calls++;
                return HttpResponse.json(baseProblemSet);
            }),
        );
        render(<ProblemSetProblemsEditorDialog problemSet={baseProblemSet} open onClose={() => {}} />);

        capturedOnDragEnd?.({ active: { id: "savchenko/1.2" }, over: { id: "savchenko/1.2" } });
        capturedOnDragEnd?.({ active: { id: "savchenko/1.2" }, over: null });
        await new Promise((r) => setTimeout(r, 400));
        expect(calls).toBe(0);
    });

    it("coalesces rapid consecutive drags into one PATCH with the final order", async () => {
        const capturedBodies: unknown[] = [];
        server.use(
            http.patch("*/api/v1/problem-sets/:shareCode", async ({ request }) => {
                capturedBodies.push(await request.json());
                return HttpResponse.json(baseProblemSet);
            }),
        );
        render(<ProblemSetProblemsEditorDialog problemSet={baseProblemSet} open onClose={() => {}} />);

        // Two drag-ends back-to-back inside the 300ms debounce window.
        // Since props don't change in this isolated test, both compute against the original
        // [1.1, 1.2, 1.3] — the SECOND drag wins via pendingReorderRef.
        capturedOnDragEnd?.({ active: { id: "savchenko/1.1" }, over: { id: "savchenko/1.2" } });
        capturedOnDragEnd?.({ active: { id: "savchenko/1.3" }, over: { id: "savchenko/1.1" } });

        await waitFor(
            () => {
                expect(capturedBodies).toHaveLength(1);
                expect(capturedBodies[0]).toEqual({
                    problemKeys: ["savchenko/1.3", "savchenko/1.1", "savchenko/1.2"],
                });
            },
            { timeout: 1500 },
        );
    });

    it("shows the saving indicator from drag-end until the PATCH completes, then hides it", async () => {
        server.use(
            http.patch("*/api/v1/problem-sets/:shareCode", async () => {
                // Delay long enough that the indicator is observable mid-flight.
                await new Promise((r) => setTimeout(r, 200));
                return HttpResponse.json(baseProblemSet);
            }),
        );
        render(<ProblemSetProblemsEditorDialog problemSet={baseProblemSet} open onClose={() => {}} />);

        // We query by the "Saving" accessible name so we don't conflict with the picker's
        // own initial CircularProgress (which also has role=progressbar).
        const savingQuery = { name: "Saving" } as const;
        expect(screen.queryByRole("progressbar", savingQuery)).not.toBeInTheDocument();

        // Drag-end should make the indicator appear during the 300ms debounce window
        // and stay visible through the in-flight PATCH.
        capturedOnDragEnd?.({ active: { id: "savchenko/1.1" }, over: { id: "savchenko/1.3" } });

        await waitFor(() => {
            expect(screen.getByRole("progressbar", savingQuery)).toBeInTheDocument();
        });

        // After the PATCH resolves and the cache invalidate refetches, the indicator goes away.
        await waitFor(
            () => {
                expect(screen.queryByRole("progressbar", savingQuery)).not.toBeInTheDocument();
            },
            { timeout: 2000 },
        );
    });

    it("shows the saving indicator immediately for a remove (no debounce)", async () => {
        server.use(
            http.patch("*/api/v1/problem-sets/:shareCode", async () => {
                await new Promise((r) => setTimeout(r, 200));
                return HttpResponse.json(baseProblemSet);
            }),
        );
        render(<ProblemSetProblemsEditorDialog problemSet={baseProblemSet} open onClose={() => {}} />);

        await userEvent.click(screen.getAllByRole("button", { name: "Remove from set" })[0]);

        await waitFor(() => {
            expect(screen.getByRole("progressbar", { name: "Saving" })).toBeInTheDocument();
        });
    });

    it("rolls back the optimistic removal when the PATCH fails", async () => {
        server.use(http.patch("*/api/v1/problem-sets/:shareCode", () => HttpResponse.json(null, { status: 500 })));
        render(<ProblemSetProblemsEditorDialog problemSet={baseProblemSet} open onClose={() => {}} />);

        // The row exists before the click
        expect(screen.getByText("savchenko/1.1")).toBeInTheDocument();
        await userEvent.click(screen.getAllByRole("button", { name: "Remove from set" })[0]);

        // After the failed PATCH + onSettled invalidate, the cache reverts to the original list
        await waitFor(() => {
            expect(screen.getByText("savchenko/1.1")).toBeInTheDocument();
        });
        expect(screen.getByText("savchenko/1.2")).toBeInTheDocument();
        expect(screen.getByText("savchenko/1.3")).toBeInTheDocument();
    });
});
