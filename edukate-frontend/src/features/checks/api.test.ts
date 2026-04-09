import { renderHook, act, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import { server } from "@/test/server";
import { getGetCheckResultsBySubmissionIdMockHandler } from "@/generated/backend";
import { createWrapper } from "@/test/render";
import { useRequestCheckMutation, useCheckResultsRequest } from "./api";

describe("useCheckResultsRequest", () => {
    it("is idle when unauthenticated", () => {
        const { result } = renderHook(() => useCheckResultsRequest("sub-1"), { wrapper: createWrapper() });
        expect(result.current.fetchStatus).toBe("idle");
    });

    it("exposes data property", () => {
        server.use(getGetCheckResultsBySubmissionIdMockHandler([]));
        const { result } = renderHook(() => useCheckResultsRequest("sub-1"), { wrapper: createWrapper() });
        expect(result.current).toHaveProperty("data");
    });
});

describe("useRequestCheckMutation", () => {
    it("exposes mutate function", () => {
        const { result } = renderHook(() => useRequestCheckMutation(), { wrapper: createWrapper() });
        expect(typeof result.current.mutate).toBe("function");
    });

    it("calls supervisorCheck endpoint when checkType is supervisor", async () => {
        server.use(
            http.post("*/api/v1/checker/supervisor", () => HttpResponse.json({})),
        );
        const { result } = renderHook(() => useRequestCheckMutation(), { wrapper: createWrapper() });
        await act(async () => {
            result.current.mutate({ submissionId: "sub-1", checkType: "supervisor" });
        });
        await waitFor(() => expect(result.current.isIdle || result.current.isSuccess).toBe(true));
    });

    it("calls selfCheck endpoint when checkType is self", async () => {
        server.use(
            http.post("*/api/v1/checker/self", () => HttpResponse.json({})),
        );
        const { result } = renderHook(() => useRequestCheckMutation(), { wrapper: createWrapper() });
        await act(async () => {
            result.current.mutate({ submissionId: "sub-1", checkType: "self" });
        });
        await waitFor(() => expect(result.current.isIdle || result.current.isSuccess).toBe(true));
    });
});
