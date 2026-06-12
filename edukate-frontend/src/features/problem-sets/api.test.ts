import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import { createWrapper } from "@/test/render";
import { server } from "@/test/server";
import {
    getGetProblemSetByShareCodeMockHandler,
    getGetProblemSetByShareCodeResponseMock,
    getUpdateSettingsMockHandler,
    getUpdateSettingsResponseMock,
} from "@/generated/backend";
import { useProblemSetRequest, useProblemSetUpdateSettingsMutation } from "./api";

describe("useProblemSetRequest", () => {
    it("does not fetch when problemSetCode is undefined", () => {
        const { result } = renderHook(() => useProblemSetRequest(undefined), { wrapper: createWrapper() });
        expect(result.current.fetchStatus).toBe("idle");
        expect(result.current.data).toBeUndefined();
    });

    it("fetches problem set data on mount", async () => {
        server.use(
            getGetProblemSetByShareCodeMockHandler(getGetProblemSetByShareCodeResponseMock({ name: "my-problem-set" })),
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

describe("useProblemSetUpdateSettingsMutation", () => {
    it("PATCHes the new name", async () => {
        let capturedBody: unknown = null;
        server.use(
            http.patch("*/api/v1/problem-sets/:shareCode", async ({ request }) => {
                capturedBody = await request.json();
                return HttpResponse.json(getUpdateSettingsResponseMock({ name: "renamed", shareCode: "code-abc" }));
            }),
        );

        const { result } = renderHook(() => useProblemSetUpdateSettingsMutation(), { wrapper: createWrapper() });
        const mutated = await result.current.mutateAsync({ shareCode: "code-abc", data: { name: "renamed" } });

        expect(capturedBody).toEqual({ name: "renamed" });
        expect(mutated.name).toBe("renamed");
    });

    it("PATCHes a partial body that only touches isPublic", async () => {
        let capturedBody: unknown = null;
        server.use(
            getUpdateSettingsMockHandler(async ({ request }) => {
                capturedBody = await request.json();
                return getUpdateSettingsResponseMock({ isPublic: true });
            }),
        );

        const { result } = renderHook(() => useProblemSetUpdateSettingsMutation(), { wrapper: createWrapper() });
        await result.current.mutateAsync({ shareCode: "code-abc", data: { isPublic: true } });

        expect(capturedBody).toEqual({ isPublic: true });
    });
});
