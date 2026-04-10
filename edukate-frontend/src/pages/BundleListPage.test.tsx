import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { server } from "@/test/server";
import {
    getGetJoinedBundlesMockHandler,
    getGetOwnedBundlesMockHandler,
    getGetPublicBundlesMockHandler,
} from "@/generated/backend";
import BundleListPage from "./BundleListPage";

// Return empty lists so BundleEmptyList renders instead of BundleCard,
// keeping tab-switch smoke tests focused on navigation behaviour only.
const EMPTY_BUNDLE_HANDLERS = [
    getGetJoinedBundlesMockHandler([]),
    getGetOwnedBundlesMockHandler([]),
    getGetPublicBundlesMockHandler([]),
];

describe("BundleListPage", () => {
    it("renders the heading", () => {
        render(<BundleListPage />);
        expect(screen.getByRole("heading", { name: /problem bundles/i })).toBeInTheDocument();
    });

    it("renders all four tabs", () => {
        render(<BundleListPage />);
        expect(screen.getByRole("tab", { name: /info/i })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: /joined/i })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: /owned/i })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: /public/i })).toBeInTheDocument();
    });

    it("shows the info tab content by default", () => {
        render(<BundleListPage />);
        expect(screen.getByRole("tab", { name: /info/i })).toHaveAttribute("aria-selected", "true");
        // BundleInfoCards unique static text
        expect(screen.getByText(/hit the road/i)).toBeInTheDocument();
    });

    it("switches to the Joined tab on click", async () => {
        server.use(...EMPTY_BUNDLE_HANDLERS);
        render(<BundleListPage />);
        await userEvent.click(screen.getByRole("tab", { name: /joined/i }));
        expect(screen.getByRole("tab", { name: /joined/i })).toHaveAttribute("aria-selected", "true");
        expect(screen.getByRole("tab", { name: /info/i })).toHaveAttribute("aria-selected", "false");
    });

    it("switches to the Public tab on click", async () => {
        server.use(...EMPTY_BUNDLE_HANDLERS);
        render(<BundleListPage />);
        await userEvent.click(screen.getByRole("tab", { name: /public/i }));
        expect(screen.getByRole("tab", { name: /public/i })).toHaveAttribute("aria-selected", "true");
    });
});
