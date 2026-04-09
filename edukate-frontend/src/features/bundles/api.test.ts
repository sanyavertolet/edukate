import { renderHook, waitFor } from "@testing-library/react";
import { createWrapper } from "@/test/render";
import { server } from "@/test/server";
import { getGetBundleByShareCodeMockHandler, getGetBundleByShareCodeResponseMock } from "@/generated/backend";
import { useBundleRequest } from "./api";

describe("useBundleRequest", () => {
    it("does not fetch when bundleCode is undefined", () => {
        const { result } = renderHook(() => useBundleRequest(undefined), { wrapper: createWrapper() });
        expect(result.current.fetchStatus).toBe("idle");
        expect(result.current.data).toBeUndefined();
    });

    it("fetches bundle data on mount", async () => {
        server.use(getGetBundleByShareCodeMockHandler(getGetBundleByShareCodeResponseMock({ name: "my-bundle" })));
        const { result } = renderHook(() => useBundleRequest("code-abc"), { wrapper: createWrapper() });
        await waitFor(() => expect(result.current.isSuccess).toBe(true));
        expect(result.current.data?.name).toBe("my-bundle");
    });

    it("surfaces all pinned fields correctly", async () => {
        server.use(
            getGetBundleByShareCodeMockHandler(
                getGetBundleByShareCodeResponseMock({ shareCode: "code-xyz", isPublic: true }),
            ),
        );
        const { result } = renderHook(() => useBundleRequest("code-xyz"), { wrapper: createWrapper() });
        await waitFor(() => expect(result.current.isSuccess).toBe(true));
        expect(result.current.data?.shareCode).toBe("code-xyz");
        expect(result.current.data?.isPublic).toBe(true);
    });
});
