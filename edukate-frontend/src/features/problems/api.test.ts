import { renderHook, waitFor } from "@testing-library/react";
import { createWrapper } from "@/test/render";
import { server } from "@/test/server";
import { getGetProblemMockHandler, getGetProblemResponseMock } from "@/generated/backend";
import { useProblemRequest } from "./api";

describe("useProblemRequest", () => {
    it("does not fetch when bookSlug is undefined", () => {
        const { result } = renderHook(() => useProblemRequest(undefined, undefined), { wrapper: createWrapper() });
        expect(result.current.fetchStatus).toBe("idle");
        expect(result.current.data).toBeUndefined();
    });

    it("fetches problem data on mount", async () => {
        server.use(getGetProblemMockHandler(getGetProblemResponseMock({ bookSlug: "savchenko", code: "1.1.1" })));
        const { result } = renderHook(() => useProblemRequest("savchenko", "1.1.1"), { wrapper: createWrapper() });
        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });
        expect(result.current.data?.code).toBe("1.1.1");
    });

    it("surfaces all pinned fields correctly", async () => {
        server.use(
            getGetProblemMockHandler(
                getGetProblemResponseMock({ bookSlug: "savchenko", code: "1.1.2", isHard: true, tags: ["algebra"] }),
            ),
        );
        const { result } = renderHook(() => useProblemRequest("savchenko", "1.1.2"), { wrapper: createWrapper() });
        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });
        expect(result.current.data?.isHard).toBe(true);
        expect(result.current.data?.tags).toContain("algebra");
    });
});
