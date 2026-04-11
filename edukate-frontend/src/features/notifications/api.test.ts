import { renderHook } from "@testing-library/react";
import { server } from "@/test/server";
import { getGetNotificationsMockHandler, getGetNotificationsResponseMock } from "@/generated/notifier";
import { createWrapper } from "@/test/render";
import {
    useGetNotificationsRequest,
    useMarkNotificationsAsReadMutation,
    useMarkAllNotificationsAsReadMutation,
} from "./api";

describe("useGetNotificationsRequest", () => {
    it("is idle when unauthenticated", () => {
        const { result } = renderHook(() => useGetNotificationsRequest(), { wrapper: createWrapper() });
        expect(result.current.fetchStatus).toBe("idle");
    });

    it("accepts isRead, page and size parameters without throwing", () => {
        server.use(getGetNotificationsMockHandler(getGetNotificationsResponseMock()));
        const { result } = renderHook(() => useGetNotificationsRequest(false, 5, 1), {
            wrapper: createWrapper(),
        });
        expect(result.current).toHaveProperty("data");
    });
});

describe("useMarkNotificationsAsReadMutation", () => {
    it("exposes mutate function", () => {
        const { result } = renderHook(() => useMarkNotificationsAsReadMutation(), { wrapper: createWrapper() });
        expect(typeof result.current.mutate).toBe("function");
    });
});

describe("useMarkAllNotificationsAsReadMutation", () => {
    it("exposes mutate function", () => {
        const { result } = renderHook(() => useMarkAllNotificationsAsReadMutation(), {
            wrapper: createWrapper(),
        });
        expect(typeof result.current.mutate).toBe("function");
    });
});
