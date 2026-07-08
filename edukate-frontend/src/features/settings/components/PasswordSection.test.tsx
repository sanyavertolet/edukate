import { describe, it, expect } from "vitest";
import { render, screen } from "@/test/render";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { server } from "@/test/server";
import { getChangePasswordMockHandler } from "@/generated/gateway";
import { PasswordSection } from "./PasswordSection";

const fillValidForm = async () => {
    await userEvent.click(screen.getByRole("button", { name: /change password/i }));
    await userEvent.type(screen.getByLabelText("Current password"), "oldpass1");
    await userEvent.type(screen.getByLabelText("New password"), "brandnew123");
    await userEvent.type(screen.getByLabelText("Confirm new password"), "brandnew123");
};

describe("PasswordSection", () => {
    it("stays collapsed until the change button is clicked", async () => {
        render(<PasswordSection />);
        expect(screen.getByRole("button", { name: /change password/i })).toBeInTheDocument();
        expect(screen.queryByLabelText("Current password")).not.toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: /change password/i }));
        expect(screen.getByLabelText("Current password")).toBeInTheDocument();
    });

    it("shows the live requirement checklist once a new password is typed", async () => {
        render(<PasswordSection />);
        await userEvent.click(screen.getByRole("button", { name: /change password/i }));
        await userEvent.type(screen.getByLabelText("New password"), "abc");
        expect(screen.getByText("6–128 characters")).toBeInTheDocument();
        expect(screen.getByText(/different from current password/i)).toBeInTheDocument();
    });

    it("reveals the password when the show toggle is clicked", async () => {
        render(<PasswordSection />);
        await userEvent.click(screen.getByRole("button", { name: /change password/i }));
        const currentField = screen.getByLabelText("Current password");
        expect(currentField).toHaveAttribute("type", "password");
        await userEvent.click(screen.getAllByRole("button", { name: /show password/i })[0]);
        expect(currentField).toHaveAttribute("type", "text");
    });

    it("keeps Update disabled until the form is valid", async () => {
        render(<PasswordSection />);
        await userEvent.click(screen.getByRole("button", { name: /change password/i }));
        const update = screen.getByRole("button", { name: /update password/i });
        expect(update).toBeDisabled();
        await userEvent.type(screen.getByLabelText("Current password"), "oldpass1");
        await userEvent.type(screen.getByLabelText("New password"), "brandnew123");
        await userEvent.type(screen.getByLabelText("Confirm new password"), "brandnew123");
        expect(update).toBeEnabled();
    });

    it("submits and shows a success message, then collapses", async () => {
        server.use(getChangePasswordMockHandler());
        render(<PasswordSection />);
        await fillValidForm();
        await userEvent.click(screen.getByRole("button", { name: /update password/i }));

        expect(await screen.findByText(/password updated/i)).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /change password/i })).toBeInTheDocument();
    });

    it("surfaces an incorrect current password from a 400 response", async () => {
        server.use(http.post("*/api/v1/users/me/password", () => HttpResponse.json(null, { status: 400 })));
        render(<PasswordSection />);
        await fillValidForm();
        await userEvent.click(screen.getByRole("button", { name: /update password/i }));

        expect(await screen.findByText(/current password is incorrect/i)).toBeInTheDocument();
    });
});
