import { renderHook, waitFor, act } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import { server } from "@/test/server";
import { createWrapper } from "@/test/render";
import {
    getWhoamiMockHandler,
    getSearchMemberProblemSetsMockHandler,
    getSupervisorCheckMockHandler,
} from "@/generated/backend";
import { useSupervisorCheckForm } from "./useSupervisorCheckForm";
import type { ProblemSetMetadata } from "@/features/problem-sets/types";

const SET_A: ProblemSetMetadata = {
    name: "Mechanics Set",
    description: "Kinematics + Dynamics",
    admins: ["alice"],
    shareCode: "PS-A",
    isPublic: false,
    size: 12,
    solvedCount: 3,
    currentUserRole: "USER",
};
const SET_B: ProblemSetMetadata = {
    name: "Optics Set",
    description: "Refraction problems",
    admins: ["alice"],
    shareCode: "PS-B",
    isPublic: false,
    size: 8,
    solvedCount: 0,
    currentUserRole: "USER",
};

function authorizeAndProvide(sets = [SET_A, SET_B]) {
    server.use(
        getWhoamiMockHandler({ name: "tester", email: "t@t.io", roles: ["USER"], status: "ACTIVE" }),
        getSearchMemberProblemSetsMockHandler(sets),
    );
}

describe("useSupervisorCheckForm — initial state", () => {
    it("starts with no selected set, no supervisor, and not-valid", () => {
        authorizeAndProvide();
        const { result } = renderHook(() => useSupervisorCheckForm({ submissionId: "1", problemKey: "savchenko/1.1" }), {
            wrapper: createWrapper(),
        });
        expect(result.current.selectedProblemSet).toBeNull();
        expect(result.current.supervisorName).toBeNull();
        expect(result.current.isValid).toBe(false);
        expect(result.current.isPending).toBe(false);
    });
});

describe("useSupervisorCheckForm — onProblemSetChange", () => {
    it("resets supervisorName to null when the problem set changes", () => {
        authorizeAndProvide();
        const { result } = renderHook(() => useSupervisorCheckForm({ submissionId: "1", problemKey: "savchenko/1.1" }), {
            wrapper: createWrapper(),
        });
        act(() => {
            result.current.setSupervisorName("alice");
        });
        expect(result.current.supervisorName).toBe("alice");
        act(() => {
            result.current.onProblemSetChange(SET_A);
        });
        expect(result.current.selectedProblemSet?.shareCode).toBe("PS-A");
        expect(result.current.supervisorName).toBeNull();
    });
});

describe("useSupervisorCheckForm — isValid", () => {
    it("is true only when both the set and supervisor are picked", () => {
        authorizeAndProvide();
        const { result } = renderHook(() => useSupervisorCheckForm({ submissionId: "1", problemKey: "savchenko/1.1" }), {
            wrapper: createWrapper(),
        });
        expect(result.current.isValid).toBe(false);

        act(() => {
            result.current.onProblemSetChange(SET_A);
        });
        expect(result.current.isValid).toBe(false);

        act(() => {
            result.current.setSupervisorName("alice");
        });
        expect(result.current.isValid).toBe(true);
    });
});

describe("useSupervisorCheckForm — auto-seed", () => {
    it("pre-selects the matching set when defaultProblemSetCode is provided", async () => {
        authorizeAndProvide();
        const { result } = renderHook(
            () =>
                useSupervisorCheckForm({
                    submissionId: "1",
                    problemKey: "savchenko/1.1",
                    defaultProblemSetCode: "PS-B",
                }),
            { wrapper: createWrapper() },
        );
        await waitFor(() => {
            expect(result.current.selectedProblemSet?.shareCode).toBe("PS-B");
        });
    });

    it("does NOT re-seed after the user clears the field", async () => {
        authorizeAndProvide();
        const { result } = renderHook(
            () =>
                useSupervisorCheckForm({
                    submissionId: "1",
                    problemKey: "savchenko/1.1",
                    defaultProblemSetCode: "PS-A",
                }),
            { wrapper: createWrapper() },
        );
        await waitFor(() => {
            expect(result.current.selectedProblemSet?.shareCode).toBe("PS-A");
        });
        // User clears the field
        act(() => {
            result.current.onProblemSetChange(null);
        });
        expect(result.current.selectedProblemSet).toBeNull();
        // Wait an extra tick and confirm the auto-seed didn't fire again
        await new Promise((r) => setTimeout(r, 50));
        expect(result.current.selectedProblemSet).toBeNull();
    });

    it("does not seed when defaultProblemSetCode matches no candidate set", async () => {
        authorizeAndProvide();
        const { result } = renderHook(
            () =>
                useSupervisorCheckForm({
                    submissionId: "1",
                    problemKey: "savchenko/1.1",
                    defaultProblemSetCode: "DOES-NOT-EXIST",
                }),
            { wrapper: createWrapper() },
        );
        // Give the fetch + would-have-seeded effect a tick
        await new Promise((r) => setTimeout(r, 50));
        expect(result.current.selectedProblemSet).toBeNull();
    });
});

describe("useSupervisorCheckForm — handleSubmit", () => {
    it("is a no-op when the form is invalid", async () => {
        authorizeAndProvide();
        let postCount = 0;
        server.use(
            http.post("*/api/v1/checker/supervisor", () => {
                postCount++;
                return new HttpResponse(null, { status: 200 });
            }),
        );
        const { result } = renderHook(() => useSupervisorCheckForm({ submissionId: "1", problemKey: "savchenko/1.1" }), {
            wrapper: createWrapper(),
        });
        act(() => {
            result.current.handleSubmit();
        });
        // Let any microtasks settle
        await new Promise((r) => setTimeout(r, 20));
        expect(postCount).toBe(0);
    });

    it("fires the mutation and calls onSubmitted on success", async () => {
        authorizeAndProvide();
        server.use(getSupervisorCheckMockHandler());
        const onSubmitted = vi.fn();
        const { result } = renderHook(
            () =>
                useSupervisorCheckForm({
                    submissionId: "42",
                    problemKey: "savchenko/1.1",
                    onSubmitted,
                }),
            { wrapper: createWrapper() },
        );
        act(() => {
            result.current.onProblemSetChange(SET_A);
        });
        act(() => {
            result.current.setSupervisorName("alice");
        });
        expect(result.current.isValid).toBe(true);
        act(() => {
            result.current.handleSubmit();
        });
        await waitFor(() => {
            expect(onSubmitted).toHaveBeenCalledTimes(1);
        });
    });
});
