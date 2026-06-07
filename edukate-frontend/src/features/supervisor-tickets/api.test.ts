import { describe, it, expect } from "vitest";
import { renderHook } from "@testing-library/react";
import { createWrapper } from "@/test/render";
import { server } from "@/test/server";
import { getGetMySupervisorTicketsResponseMock, getGetMySupervisorTicketsMockHandler } from "@/generated/backend";
import { useSupervisorTicketsQuery, useSubmitVerdictMutation } from "./api";

describe("useSupervisorTicketsQuery", () => {
    it("is idle when unauthenticated", () => {
        const { result } = renderHook(() => useSupervisorTicketsQuery("SHARE01"), {
            wrapper: createWrapper(),
        });
        expect(result.current.fetchStatus).toBe("idle");
    });

    it("exposes data property", () => {
        server.use(getGetMySupervisorTicketsMockHandler(getGetMySupervisorTicketsResponseMock()));
        const { result } = renderHook(() => useSupervisorTicketsQuery("SHARE01"), {
            wrapper: createWrapper(),
        });
        expect(result.current).toHaveProperty("data");
    });
});

describe("useSubmitVerdictMutation", () => {
    it("exposes mutate function", () => {
        const { result } = renderHook(() => useSubmitVerdictMutation("SHARE01", 0, 20), {
            wrapper: createWrapper(),
        });
        expect(result.current.mutate).toBeDefined();
    });
});
