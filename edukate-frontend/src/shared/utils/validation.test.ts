import { validate } from "./validation";

describe("validate('username', ...)", () => {
    it("returns null for a valid username", () => {
        expect(validate("username", "alice123")).toBeNull();
    });

    it("accepts a username with a hyphen in the middle", () => {
        expect(validate("username", "al-ice1")).toBeNull();
    });

    it("accepts a username with an underscore in the middle", () => {
        expect(validate("username", "al_ice1")).toBeNull();
    });

    it("accepts a username of exactly 3 characters", () => {
        expect(validate("username", "abc")).toBeNull();
    });

    it("accepts a username of exactly 15 characters", () => {
        expect(validate("username", "abcdefghijklmn1")).toBeNull();
    });

    it("rejects a username shorter than 3 characters", () => {
        expect(validate("username", "ab")).not.toBeNull();
    });

    it("rejects a username longer than 15 characters", () => {
        expect(validate("username", "abcdefghijklmno1")).not.toBeNull();
    });

    it("returns username_start_error when username starts with a digit", () => {
        expect(validate("username", "1alice1")).toBe("username_start_error");
    });

    it("returns username_start_error when username starts with an underscore", () => {
        expect(validate("username", "_alice1")).toBe("username_start_error");
    });

    it("returns username_start_error when username starts with @", () => {
        expect(validate("username", "@alice1")).toBe("username_start_error");
    });

    it("returns username_end_error when username ends with an underscore", () => {
        expect(validate("username", "alice_")).toBe("username_end_error");
    });

    it("returns username_end_error when username ends with a hyphen", () => {
        expect(validate("username", "alice-")).toBe("username_end_error");
    });

    it("returns username_chars_error when username contains @", () => {
        expect(validate("username", "ali@ce")).toBe("username_chars_error");
    });

    it("returns username_chars_error when username contains a space", () => {
        expect(validate("username", "alice bob")).toBe("username_chars_error");
    });

    it("returns username_chars_error when username contains ! in the middle", () => {
        expect(validate("username", "ali!ce1")).toBe("username_chars_error");
    });
});

describe("validate('email', ...)", () => {
    it("returns null for a valid email", () => {
        expect(validate("email", "user@example.com")).toBeNull();
    });

    it("returns null for an email with a subdomain", () => {
        expect(validate("email", "user@mail.example.com")).toBeNull();
    });

    it("rejects an email with no @ symbol", () => {
        expect(validate("email", "userexample.com")).not.toBeNull();
    });

    it("rejects an email with no local part", () => {
        expect(validate("email", "@example.com")).not.toBeNull();
    });

    it("rejects an email with no domain", () => {
        expect(validate("email", "user@")).not.toBeNull();
    });

    it("rejects an email with no TLD separator", () => {
        expect(validate("email", "user@example")).not.toBeNull();
    });

    it("rejects an email containing a space", () => {
        expect(validate("email", "us er@example.com")).not.toBeNull();
    });
});

describe("validate('password', ...)", () => {
    it("returns null for a valid password", () => {
        expect(validate("password", "securePass1")).toBeNull();
    });

    it("accepts a password of exactly 6 characters", () => {
        expect(validate("password", "abc123")).toBeNull();
    });

    it("accepts a password of exactly 128 characters", () => {
        expect(validate("password", "a".repeat(128))).toBeNull();
    });

    it("rejects a password shorter than 6 characters", () => {
        expect(validate("password", "abc")).not.toBeNull();
    });

    it("returns password_length_error for a password longer than 128 characters", () => {
        expect(validate("password", "a".repeat(129))).toBe("password_length_error");
    });

    it("rejects a password that is entirely whitespace (trim makes it too short)", () => {
        expect(validate("password", "      ")).not.toBeNull();
    });
});
