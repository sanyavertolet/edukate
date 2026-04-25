import { render, screen } from "@/test/render";
import { UserAvatar } from "./UserAvatar";
import { getColorByStringHash } from "@/shared/utils/utils";

describe("UserAvatar", () => {
    it("renders first 2 letters of the name", () => {
        render(<UserAvatar name="alice" />);
        expect(screen.getByText("al")).toBeInTheDocument();
    });

    it("applies color from getColorByStringHash", () => {
        render(<UserAvatar name="bob" />);
        const avatar = screen.getByText("bo").closest(".MuiAvatar-root");
        expect(avatar).toHaveStyle({ backgroundColor: getColorByStringHash("bob") });
    });

    it("renders medium size (32px) by default", () => {
        render(<UserAvatar name="charlie" />);
        const avatar = screen.getByText("ch").closest(".MuiAvatar-root");
        expect(avatar).toHaveStyle({ width: "32px", height: "32px" });
    });

    it("renders small size (28px) when size is small", () => {
        render(<UserAvatar name="dave" size="small" />);
        const avatar = screen.getByText("da").closest(".MuiAvatar-root");
        expect(avatar).toHaveStyle({ width: "28px", height: "28px" });
    });

    it("shows outline ring when highlighted", () => {
        render(<UserAvatar name="eve" highlighted />);
        const avatar = screen.getByText("ev").closest(".MuiAvatar-root");
        expect(avatar).toHaveStyle({ outline: "2px solid" });
    });

    it("does not show outline when not highlighted", () => {
        render(<UserAvatar name="frank" />);
        const avatar = screen.getByText("fr").closest(".MuiAvatar-root");
        expect(avatar).not.toHaveStyle({ outline: "2px solid" });
    });
});
