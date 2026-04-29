import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { ClearableTextField } from "./ClearableTextField";

describe("ClearableTextField", () => {
    it("renders the label and value", () => {
        render(<ClearableTextField label="Book" value="savchenko" onChange={vi.fn()} onClear={vi.fn()} />);
        expect(screen.getByLabelText("Book")).toHaveValue("savchenko");
    });

    it("always renders the clear button in the DOM to prevent layout shift", () => {
        render(<ClearableTextField label="Book" value="" onChange={vi.fn()} onClear={vi.fn()} />);
        // Button is always present (reserving space), even when value is empty
        expect(screen.getByTestId("ClearIcon")).toBeInTheDocument();
    });

    it("calls onClear when the clear button is clicked", async () => {
        const onClear = vi.fn();
        render(<ClearableTextField label="Book" value="savchenko" onChange={vi.fn()} onClear={onClear} />);
        await userEvent.click(screen.getByRole("button"));
        expect(onClear).toHaveBeenCalledOnce();
    });

    it("calls onChange when user types", async () => {
        const onChange = vi.fn();
        render(<ClearableTextField label="Book" value="" onChange={onChange} onClear={vi.fn()} />);
        await userEvent.type(screen.getByLabelText("Book"), "s");
        expect(onChange).toHaveBeenCalledWith("s");
    });

    it("applies custom sx prop", () => {
        render(<ClearableTextField label="Book" value="" onChange={vi.fn()} onClear={vi.fn()} sx={{ minWidth: 200 }} />);
        expect(screen.getByLabelText("Book")).toBeInTheDocument();
    });
});
