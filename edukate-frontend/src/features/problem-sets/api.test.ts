import { renderHook, waitFor } from "@testing-library/react";
import { createWrapper } from "@/test/render";
import { server } from "@/test/server";
import { getGetProblemSetByShareCodeMockHandler, getGetProblemSetByShareCodeResponseMock } from "@/generated/backend";
import { useProblemSetRequest } from "./api";

describe("useProblemSetRequest", () => {
    it("does not fetch when problemSetCode is undefined", () => {
        const { result } = renderHook(() => useProblemSetRequest(undefined), { wrapper: createWrapper() });
        expect(result.current.fetchStatus).toBe("idle");
        expect(result.current.data).toBeUndefined();
    });

    it("fetches problem set data on mount", async () => {
        server.use(
            getGetProblemSetByShareCodeMockHandler(
                getGetProblemSetByShareCodeResponseMock({ name: "my-problem-set" }),
            ),
        );
        const { result } = renderHook(() => useProblemSetRequest("code-abc"), { wrapper: createWrapper() });
        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });
        expect(result.current.data?.name).toBe("my-problem-set");
    });

    it("surfaces all pinned fields correctly", async () => {
        server.use(
            getGetProblemSetByShareCodeMockHandler(
                getGetProblemSetByShareCodeResponseMock({ shareCode: "code-xyz", isPublic: true }),
            ),
        );
        const { result } = renderHook(() => useProblemSetRequest("code-xyz"), { wrapper: createWrapper() });
        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });
        expect(result.current.data?.shareCode).toBe("code-xyz");
        expect(result.current.data?.isPublic).toBe(true);
    });
});
