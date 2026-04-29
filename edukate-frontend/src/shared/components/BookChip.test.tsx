import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { BookChip } from "./BookChip";

describe("BookChip", () => {
    it("renders the book slug as text", () => {
        render(<BookChip bookSlug="savchenko" />);
        expect(screen.getByText("savchenko")).toBeInTheDocument();
    });

    it("applies primary color to the text", () => {
        render(<BookChip bookSlug="savchenko" />);
        const el = screen.getByText("savchenko");
        expect(el).toBeInTheDocument();
    });

    it("calls onClick with the book slug when clicked", async () => {
        const onClick = vi.fn();
        render(<BookChip bookSlug="savchenko" onClick={onClick} />);
        await userEvent.click(screen.getByText("savchenko"));
        expect(onClick).toHaveBeenCalledWith("savchenko");
    });

    it("shows pointer cursor when onClick is provided", () => {
        render(<BookChip bookSlug="savchenko" onClick={vi.fn()} />);
        expect(screen.getByText("savchenko")).toHaveStyle({ cursor: "pointer" });
    });

    it("shows default cursor when onClick is not provided", () => {
        render(<BookChip bookSlug="savchenko" />);
        expect(screen.getByText("savchenko")).toHaveStyle({ cursor: "default" });
    });

    it("stops event propagation on click to prevent row click", async () => {
        const rowClick = vi.fn();
        const chipClick = vi.fn();
        render(
            <div onClick={rowClick}>
                <BookChip bookSlug="savchenko" onClick={chipClick} />
            </div>,
        );
        await userEvent.click(screen.getByText("savchenko"));
        expect(chipClick).toHaveBeenCalledOnce();
        expect(rowClick).not.toHaveBeenCalled();
    });
});
