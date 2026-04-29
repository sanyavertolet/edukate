import userEvent from "@testing-library/user-event";
import { render as rtlRender, screen, act, waitFor } from "@testing-library/react";
import { FC, ReactNode } from "react";
import { PwaProvider } from "@/shared/context/PwaContext";
import { InstallPrompt } from "./InstallPrompt";

const wrapper: FC<{ children: ReactNode }> = ({ children }) => <PwaProvider>{children}</PwaProvider>;

function renderInstallPrompt() {
    return rtlRender(<InstallPrompt />, { wrapper });
}

describe("InstallPrompt", () => {
    it("is not visible when no beforeinstallprompt has fired", () => {
        renderInstallPrompt();
        expect(screen.queryByText(/install edukate/i)).not.toBeInTheDocument();
    });

    it("is visible after beforeinstallprompt fires", () => {
        renderInstallPrompt();

        act(() => {
            window.dispatchEvent(new Event("beforeinstallprompt", { cancelable: true }));
        });

        expect(screen.getByText(/install edukate/i)).toBeInTheDocument();
    });

    it("has an Install action button", () => {
        renderInstallPrompt();

        act(() => {
            window.dispatchEvent(new Event("beforeinstallprompt", { cancelable: true }));
        });

        expect(screen.getByRole("button", { name: /install/i })).toBeInTheDocument();
    });

    it("disappears when dismissed via the close button", async () => {
        renderInstallPrompt();

        act(() => {
            window.dispatchEvent(new Event("beforeinstallprompt", { cancelable: true }));
        });
        expect(screen.getByText(/install edukate/i)).toBeInTheDocument();

        const closeButton = screen.getByTestId("CloseIcon").closest("button");
        expect(closeButton).not.toBeNull();
        await userEvent.click(closeButton as HTMLElement);

        await waitFor(() => {
            expect(screen.queryByText(/install edukate/i)).not.toBeInTheDocument();
        });
    });
});
