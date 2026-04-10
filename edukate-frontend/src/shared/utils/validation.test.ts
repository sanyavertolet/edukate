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

    it("rejects a username that starts with a digit", () => {
        expect(validate("username", "1alice")).not.toBeNull();
    });

    it("rejects a username that starts with an underscore", () => {
        expect(validate("username", "_alice1")).not.toBeNull();
    });

    it("rejects a username that ends with an underscore", () => {
        expect(validate("username", "alice_")).not.toBeNull();
    });

    it("rejects a username that ends with a hyphen", () => {
        expect(validate("username", "alice-")).not.toBeNull();
    });

    it("rejects a username containing a space", () => {
        expect(validate("username", "alice bob")).not.toBeNull();
    });

    it("rejects a username containing a special character", () => {
        expect(validate("username", "alice!")).not.toBeNull();
    });

    it("rejects a username that is all whitespace (trim makes it too short)", () => {
        expect(validate("username", "      ")).not.toBeNull();
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

    it("accepts a password of exactly 20 characters", () => {
        expect(validate("password", "a".repeat(20))).toBeNull();
    });

    it("rejects a password shorter than 6 characters", () => {
        expect(validate("password", "abc")).not.toBeNull();
    });

    it("rejects a password longer than 20 characters", () => {
        expect(validate("password", "a".repeat(21))).not.toBeNull();
    });

    it("rejects a password that is entirely whitespace (trim makes it too short)", () => {
        expect(validate("password", "      ")).not.toBeNull();
    });
});
