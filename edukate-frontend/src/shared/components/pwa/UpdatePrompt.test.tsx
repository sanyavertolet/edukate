import userEvent from "@testing-library/user-event";
import { render as rtlRender, screen, act, waitFor } from "@testing-library/react";
import { FC, ReactNode } from "react";
import { PwaProvider } from "@/shared/context/PwaContext";
import { UpdatePrompt } from "./UpdatePrompt";

const wrapper: FC<{ children: ReactNode }> = ({ children }) => <PwaProvider>{children}</PwaProvider>;

function renderUpdatePrompt() {
    return rtlRender(<UpdatePrompt />, { wrapper });
}

describe("UpdatePrompt", () => {
    it("is not visible when no update is available", () => {
        renderUpdatePrompt();
        expect(screen.queryByText(/new version/i)).not.toBeInTheDocument();
    });

    it("is visible after sw-update-available fires", () => {
        renderUpdatePrompt();

        act(() => {
            window.dispatchEvent(new CustomEvent("sw-update-available"));
        });

        expect(screen.getByText(/new version/i)).toBeInTheDocument();
    });

    it("has an Update action button", () => {
        renderUpdatePrompt();

        act(() => {
            window.dispatchEvent(new CustomEvent("sw-update-available"));
        });

        expect(screen.getByRole("button", { name: /update/i })).toBeInTheDocument();
    });

    it("disappears when dismissed via the close button", async () => {
        renderUpdatePrompt();

        act(() => {
            window.dispatchEvent(new CustomEvent("sw-update-available"));
        });
        expect(screen.getByText(/new version/i)).toBeInTheDocument();

        const closeButton = screen.getByTestId("CloseIcon").closest("button");
        expect(closeButton).not.toBeNull();
        await userEvent.click(closeButton as HTMLElement);

        await waitFor(() => {
            expect(screen.queryByText(/new version/i)).not.toBeInTheDocument();
        });
    });
});
