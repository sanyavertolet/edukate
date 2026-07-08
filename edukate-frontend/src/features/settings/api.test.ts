import { describe, it, expect } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import { createWrapper } from "@/test/render";
import { server } from "@/test/server";
import { useUploadAvatarMutation, useDeleteAvatarMutation } from "./api";

describe("useUploadAvatarMutation", () => {
    it("PUTs to the avatar endpoint and returns the new avatar URL", async () => {
        // NB: axios' XHR adapter through MSW does not reconstruct the multipart body in
        // jsdom, so we assert the round-trip (endpoint hit + returned URL) rather than the
        // multipart parts; the "content" field is enforced by the backend controller test.
        let method: string | null = null;
        const url = "http://cdn.example.com/edukate/users/42/avatar/avatar.jpg?v=1700000000";
        server.use(
            http.put("*/api/v1/users/me/avatar", ({ request }) => {
                method = request.method;
                return HttpResponse.text(url);
            }),
        );

        const { result } = renderHook(() => useUploadAvatarMutation(), { wrapper: createWrapper() });
        const blob = new Blob([new Uint8Array(16)], { type: "image/jpeg" });
        const returned = await result.current.mutateAsync(blob);

        expect(method).toBe("PUT");
        expect(returned).toBe(url);
    });

    it("surfaces an error state when the server responds with a failure", async () => {
        server.use(http.put("*/api/v1/users/me/avatar", () => HttpResponse.json(null, { status: 500 })));

        const { result } = renderHook(() => useUploadAvatarMutation(), { wrapper: createWrapper() });
        // `mutate` (not `mutateAsync`) keeps the rejection inside React Query, which exposes it as state.
        result.current.mutate(new Blob(["x"]));
        await waitFor(() => {
            expect(result.current.isError).toBe(true);
        });
    });
});

describe("useDeleteAvatarMutation", () => {
    it("issues a DELETE to the avatar endpoint and resolves on success", async () => {
        let deleteCalled = false;
        server.use(
            http.delete("*/api/v1/users/me/avatar", () => {
                deleteCalled = true;
                return new HttpResponse(null, { status: 204 });
            }),
        );

        const { result } = renderHook(() => useDeleteAvatarMutation(), { wrapper: createWrapper() });
        result.current.mutate();

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });
        expect(deleteCalled).toBe(true);
    });
});
