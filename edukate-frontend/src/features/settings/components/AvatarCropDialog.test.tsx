import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@/test/render";
import userEvent from "@testing-library/user-event";
import { AvatarCropDialog } from "./AvatarCropDialog";

// react-easy-crop needs a real layout + decoded image, neither of which jsdom provides.
// Replace it with a button that reports a fixed pixel crop area on demand, so we can
// exercise the dialog's own open/close/enable logic deterministically.
vi.mock("react-easy-crop", () => ({
    default: ({ onCropComplete }: { onCropComplete: (percent: unknown, pixels: unknown) => void }) => (
        <button
            type="button"
            data-testid="report-crop"
            onClick={() => {
                onCropComplete({ x: 0, y: 0, width: 100, height: 100 }, { x: 0, y: 0, width: 100, height: 100 });
            }}
        >
            crop-surface
        </button>
    ),
}));

const IMAGE = "data:image/png;base64,iVBORw0KGgo=";

describe("AvatarCropDialog", () => {
    it("stays closed when no image is selected", () => {
        render(<AvatarCropDialog imageSrc={null} onClose={vi.fn()} onConfirm={vi.fn()} />);
        expect(screen.queryByText("Crop your avatar")).not.toBeInTheDocument();
    });

    it("opens with the cropper and actions once an image is provided", () => {
        render(<AvatarCropDialog imageSrc={IMAGE} onClose={vi.fn()} onConfirm={vi.fn()} />);
        expect(screen.getByText("Crop your avatar")).toBeInTheDocument();
        expect(screen.getByTestId("report-crop")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Cancel" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /save avatar/i })).toBeInTheDocument();
    });

    it("invokes onClose when Cancel is clicked", async () => {
        const onClose = vi.fn();
        render(<AvatarCropDialog imageSrc={IMAGE} onClose={onClose} onConfirm={vi.fn()} />);
        await userEvent.click(screen.getByRole("button", { name: "Cancel" }));
        expect(onClose).toHaveBeenCalledTimes(1);
    });

    it("keeps Save disabled until a crop area is reported", async () => {
        render(<AvatarCropDialog imageSrc={IMAGE} onClose={vi.fn()} onConfirm={vi.fn()} />);
        const save = screen.getByRole("button", { name: /save avatar/i });
        expect(save).toBeDisabled();

        await userEvent.click(screen.getByTestId("report-crop"));
        expect(save).toBeEnabled();
    });

    it("disables Save while a submission is in flight", () => {
        render(<AvatarCropDialog imageSrc={IMAGE} onClose={vi.fn()} onConfirm={vi.fn()} submitting />);
        expect(screen.getByRole("button", { name: /save avatar/i })).toBeDisabled();
    });
});
