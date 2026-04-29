import { render, screen, act } from "@/test/render";
import { OfflineBanner } from "./OfflineBanner";

describe("OfflineBanner", () => {
    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("shows the warning when offline", () => {
        vi.spyOn(navigator, "onLine", "get").mockReturnValue(false);
        render(<OfflineBanner />);
        expect(screen.getByRole("alert")).toBeInTheDocument();
        expect(screen.getByText(/offline/i)).toBeInTheDocument();
    });

    it("displays the banner after going offline", () => {
        vi.spyOn(navigator, "onLine", "get").mockReturnValue(true);
        render(<OfflineBanner />);

        vi.spyOn(navigator, "onLine", "get").mockReturnValue(false);
        act(() => {
            window.dispatchEvent(new Event("offline"));
        });

        expect(screen.getByRole("alert")).toBeInTheDocument();
    });

    it("has severity warning", () => {
        vi.spyOn(navigator, "onLine", "get").mockReturnValue(false);
        render(<OfflineBanner />);
        const alert = screen.getByRole("alert");
        expect(alert).toHaveClass("MuiAlert-standardWarning");
    });
});
