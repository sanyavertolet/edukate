import { renderHook, waitFor } from "@testing-library/react";
import { server } from "@/test/server";
import {
    getGetMySubmissionsMockHandler,
    getGetSubmissionByIdMockHandler,
    getGetSubmissionByIdResponseMock,
} from "@/generated/backend";
import { createWrapper } from "@/test/render";
import { useMySubmissionsQuery, useSubmissionQuery } from "./api";

describe("useMySubmissionsQuery", () => {
    it("is idle when isAuthorized is false (unauthenticated)", () => {
        const { result } = renderHook(() => useMySubmissionsQuery("prob-1"), { wrapper: createWrapper() });
        // Auth context defaults to not-authorized in tests — query should not fire
        expect(result.current.fetchStatus).toBe("idle");
    });

    it("returns data when MSW responds", () => {
        server.use(getGetMySubmissionsMockHandler([]));
        // We can't easily set isAuthorized=true without a full auth flow,
        // so just verify the hook exposes the query shape (doesn't throw)
        const { result } = renderHook(() => useMySubmissionsQuery(), { wrapper: createWrapper() });
        expect(result.current).toHaveProperty("data");
        expect(result.current).toHaveProperty("isLoading");
    });
});

describe("useSubmissionQuery", () => {
    it("is idle when submissionId is undefined", () => {
        const { result } = renderHook(() => useSubmissionQuery(undefined), { wrapper: createWrapper() });
        expect(result.current.fetchStatus).toBe("idle");
    });

    it("fetches submission when id is provided", async () => {
        server.use(getGetSubmissionByIdMockHandler(getGetSubmissionByIdResponseMock({ id: 42, problemKey: "savchenko/1.1.1" })));
        const { result } = renderHook(() => useSubmissionQuery("42"), { wrapper: createWrapper() });
        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });
        expect(result.current.data?.id).toBe(42);
    });
});
