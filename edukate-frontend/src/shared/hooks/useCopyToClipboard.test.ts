import { renderHook } from "@testing-library/react";
import { createWrapper } from "@/test/render";
import { useCopyToClipboard } from "./useCopyToClipboard";

describe("useCopyToClipboard", () => {
    it("calls navigator.clipboard.writeText with the given text", () => {
        const writeText = vi.fn().mockResolvedValue(undefined);
        Object.assign(navigator, { clipboard: { writeText } });

        const { result } = renderHook(() => useCopyToClipboard(), { wrapper: createWrapper() });
        result.current("test-text");

        expect(writeText).toHaveBeenCalledWith("test-text");
    });

    it("returns a stable callback reference", () => {
        const writeText = vi.fn().mockResolvedValue(undefined);
        Object.assign(navigator, { clipboard: { writeText } });

        const { result, rerender } = renderHook(() => useCopyToClipboard(), { wrapper: createWrapper() });
        const first = result.current;
        rerender();
        expect(result.current).toBe(first);
    });
});
