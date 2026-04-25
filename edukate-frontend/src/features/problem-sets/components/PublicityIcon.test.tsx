import { render, screen } from "@/test/render";
import { PublicityIcon } from "./PublicityIcon";

describe("PublicityIcon", () => {
    it("shows public icon for public sets", () => {
        render(<PublicityIcon isPublic={true} />);
        expect(screen.getByTestId("PublicIcon")).toBeInTheDocument();
    });

    it("shows lock icon for private sets", () => {
        render(<PublicityIcon isPublic={false} />);
        expect(screen.getByTestId("LockIcon")).toBeInTheDocument();
    });

    it("renders without tooltip when disableTooltip is true", () => {
        render(<PublicityIcon isPublic={true} disableTooltip />);
        expect(screen.getByTestId("PublicIcon")).toBeInTheDocument();
        // No tooltip wrapper — the icon is rendered directly
        expect(screen.queryByRole("tooltip")).not.toBeInTheDocument();
    });
});
