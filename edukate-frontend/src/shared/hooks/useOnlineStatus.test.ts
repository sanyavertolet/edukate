import { renderHook, act } from "@testing-library/react";
import { useOnlineStatus } from "./useOnlineStatus";

describe("useOnlineStatus", () => {
    it("returns true when the browser is online", () => {
        vi.spyOn(navigator, "onLine", "get").mockReturnValue(true);
        const { result } = renderHook(() => useOnlineStatus());
        expect(result.current).toBe(true);
    });

    it("returns false when the browser is offline", () => {
        vi.spyOn(navigator, "onLine", "get").mockReturnValue(false);
        const { result } = renderHook(() => useOnlineStatus());
        expect(result.current).toBe(false);
    });

    it("updates when the browser goes offline", () => {
        vi.spyOn(navigator, "onLine", "get").mockReturnValue(true);
        const { result } = renderHook(() => useOnlineStatus());
        expect(result.current).toBe(true);

        vi.spyOn(navigator, "onLine", "get").mockReturnValue(false);
        act(() => {
            window.dispatchEvent(new Event("offline"));
        });
        expect(result.current).toBe(false);
    });

    it("updates when the browser comes back online", () => {
        vi.spyOn(navigator, "onLine", "get").mockReturnValue(false);
        const { result } = renderHook(() => useOnlineStatus());
        expect(result.current).toBe(false);

        vi.spyOn(navigator, "onLine", "get").mockReturnValue(true);
        act(() => {
            window.dispatchEvent(new Event("online"));
        });
        expect(result.current).toBe(true);
    });

    it("cleans up event listeners on unmount", () => {
        const addSpy = vi.spyOn(window, "addEventListener");
        const removeSpy = vi.spyOn(window, "removeEventListener");

        const { unmount } = renderHook(() => useOnlineStatus());

        expect(addSpy).toHaveBeenCalledWith("online", expect.any(Function));
        expect(addSpy).toHaveBeenCalledWith("offline", expect.any(Function));

        unmount();

        expect(removeSpy).toHaveBeenCalledWith("online", expect.any(Function));
        expect(removeSpy).toHaveBeenCalledWith("offline", expect.any(Function));
    });
});
