import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@/test/render";
import { http, HttpResponse } from "msw";
import { server } from "@/test/server";
import { getWhoamiMockHandler, getWhoamiResponseMock } from "@/generated/backend";
import { ProfileSection } from "./ProfileSection";

// The crop dialog renders react-easy-crop, which needs a real layout + decoded image.
// Stub it so opening the dialog is safe in jsdom.
vi.mock("react-easy-crop", () => ({
    default: () => <div data-testid="cropper" />,
}));

// ProfileSection measures its field column via ResizeObserver, which jsdom lacks.
class ResizeObserverStub {
    observe() {}
    unobserve() {}
    disconnect() {}
}
vi.stubGlobal("ResizeObserver", ResizeObserverStub);

/** Seed the whoami query so AuthProvider exposes a concrete user. */
function seedUser(overrides: Partial<ReturnType<typeof getWhoamiResponseMock>> = {}) {
    server.use(
        getWhoamiMockHandler(
            getWhoamiResponseMock({
                name: "alice",
                email: "alice@example.com",
                roles: ["USER"],
                status: "ACTIVE",
                avatarUrl: undefined,
                ...overrides,
            }),
        ),
    );
}

/** Fire a selection on the hidden avatar file input. */
function selectAvatarFile(container: HTMLElement, file: File) {
    const input = container.querySelector('input[type="file"]') as HTMLInputElement;
    const fileList = Object.assign([file], { item: (i: number) => [file][i] ?? null }) as unknown as FileList;
    Object.defineProperty(input, "files", { value: fileList, configurable: true });
    fireEvent.change(input);
}

describe("ProfileSection", () => {
    beforeEach(() => {
        seedUser();
    });

    it("renders the current username and email once the user loads", async () => {
        render(<ProfileSection />);
        expect(await screen.findByDisplayValue("alice")).toBeInTheDocument();
        expect(screen.getByDisplayValue("alice@example.com")).toBeInTheDocument();
    });

    it("rejects a non-image pick with a shared validation error", async () => {
        const { container } = render(<ProfileSection />);
        await screen.findByDisplayValue("alice");

        selectAvatarFile(container, new File(["%PDF-"], "resume.pdf", { type: "application/pdf" }));

        expect(await screen.findByText("That file type is not supported.")).toBeInTheDocument();
        // The crop dialog must not open for a rejected file.
        expect(screen.queryByText("Crop your avatar")).not.toBeInTheDocument();
    });

    it("opens the crop dialog after picking a valid image", async () => {
        const { container } = render(<ProfileSection />);
        await screen.findByDisplayValue("alice");

        selectAvatarFile(container, new File(["fake-image-bytes"], "avatar.png", { type: "image/png" }));

        expect(await screen.findByText("Crop your avatar")).toBeInTheDocument();
        expect(screen.getByTestId("cropper")).toBeInTheDocument();
    });

    it("issues a DELETE when removing an existing avatar", async () => {
        seedUser({ avatarUrl: "http://cdn.example.com/edukate/users/1/avatar/avatar.jpg?v=1" });
        let deleteCalled = false;
        server.use(
            http.delete("*/api/v1/users/me/avatar", () => {
                deleteCalled = true;
                return new HttpResponse(null, { status: 204 });
            }),
        );

        render(<ProfileSection />);
        const removeButton = await screen.findByRole("button", { name: "Remove" });
        fireEvent.click(removeButton);

        await waitFor(() => {
            expect(deleteCalled).toBe(true);
        });
    });
});
