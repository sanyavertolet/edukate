import { renderHook, act } from "@testing-library/react";
import { FC, ReactNode } from "react";
import { PwaProvider, usePwaContext } from "./PwaContext";

const wrapper: FC<{ children: ReactNode }> = ({ children }) => <PwaProvider>{children}</PwaProvider>;

describe("PwaContext", () => {
    it("canInstall is false by default", () => {
        const { result } = renderHook(() => usePwaContext(), { wrapper });
        expect(result.current.canInstall).toBe(false);
    });

    it("updateAvailable is false by default", () => {
        const { result } = renderHook(() => usePwaContext(), { wrapper });
        expect(result.current.updateAvailable).toBe(false);
    });

    it("sets canInstall to true when beforeinstallprompt fires", () => {
        const { result } = renderHook(() => usePwaContext(), { wrapper });

        act(() => {
            const event = new Event("beforeinstallprompt", { cancelable: true });
            window.dispatchEvent(event);
        });

        expect(result.current.canInstall).toBe(true);
    });

    it("prevents default on beforeinstallprompt to suppress browser mini-infobar", () => {
        renderHook(() => usePwaContext(), { wrapper });

        let defaultPrevented = false;
        act(() => {
            const event = new Event("beforeinstallprompt", { cancelable: true });
            window.dispatchEvent(event);
            defaultPrevented = event.defaultPrevented;
        });

        expect(defaultPrevented).toBe(true);
    });

    it("sets updateAvailable to true when sw-update-available fires", () => {
        const { result } = renderHook(() => usePwaContext(), { wrapper });

        act(() => {
            window.dispatchEvent(new CustomEvent("sw-update-available"));
        });

        expect(result.current.updateAvailable).toBe(true);
    });

    it("cleans up event listeners on unmount", () => {
        const removeSpy = vi.spyOn(window, "removeEventListener");
        const { unmount } = renderHook(() => usePwaContext(), { wrapper });

        unmount();

        const removedEvents = removeSpy.mock.calls.map(([event]) => event);
        expect(removedEvents).toContain("beforeinstallprompt");
        expect(removedEvents).toContain("sw-update-available");
    });
});
