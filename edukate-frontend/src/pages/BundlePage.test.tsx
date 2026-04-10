import { renderAtPath, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getGetBundleByShareCodeMockHandler, getGetBundleByShareCodeResponseMock } from "@/generated/backend";
import BundlePage from "./BundlePage";

describe("BundlePage", () => {
    it("renders 'Index' as the selector label before data loads", () => {
        renderAtPath("/bundles/test-code", "/bundles/:code", <BundlePage />);
        expect(screen.getByText("Index")).toBeInTheDocument();
    });

    it("renders the bundle name as a heading once MSW responds", async () => {
        server.use(
            getGetBundleByShareCodeMockHandler(getGetBundleByShareCodeResponseMock({ name: "Test Bundle", problems: [] })),
        );
        renderAtPath("/bundles/test-code", "/bundles/:code", <BundlePage />);
        await waitFor(() => {
            expect(screen.getByRole("heading", { level: 1, name: "Test Bundle" })).toBeInTheDocument();
        });
    });
});
