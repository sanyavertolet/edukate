import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { ProblemLabel } from "./ProblemLabel";

describe("ProblemLabel", () => {
    it("renders only the code part (strips book slug prefix)", () => {
        render(<ProblemLabel problemKey="savchenko/1.1.7" />);
        expect(screen.getByText("1.1.7")).toBeInTheDocument();
        expect(screen.queryByText("savchenko/1.1.7")).not.toBeInTheDocument();
        expect(screen.queryByText("savchenko")).not.toBeInTheDocument();
    });

    it("handles nested codes (bookSlug/chapter/code)", () => {
        render(<ProblemLabel problemKey="irodov/1/1.1" />);
        expect(screen.getByText("1/1.1")).toBeInTheDocument();
    });

    it("calls onClick with the full problemKey (including book slug) when clicked", async () => {
        const onClick = vi.fn();
        render(<ProblemLabel problemKey="savchenko/1.1.7" onClick={onClick} />);
        await userEvent.click(screen.getByText("1.1.7"));
        expect(onClick).toHaveBeenCalledWith("savchenko/1.1.7");
    });

    it("shows pointer cursor when onClick is provided", () => {
        render(<ProblemLabel problemKey="savchenko/1.1.7" onClick={vi.fn()} />);
        expect(screen.getByText("1.1.7")).toHaveStyle({ cursor: "pointer" });
    });

    it("shows default cursor when onClick is not provided", () => {
        render(<ProblemLabel problemKey="savchenko/1.1.7" />);
        expect(screen.getByText("1.1.7")).toHaveStyle({ cursor: "default" });
    });

    it("stops event propagation on click to prevent table row click", async () => {
        const rowClick = vi.fn();
        const labelClick = vi.fn();
        render(
            <div onClick={rowClick}>
                <ProblemLabel problemKey="savchenko/1.1.7" onClick={labelClick} />
            </div>,
        );
        await userEvent.click(screen.getByText("1.1.7"));
        expect(labelClick).toHaveBeenCalledOnce();
        expect(rowClick).not.toHaveBeenCalled();
    });
});
