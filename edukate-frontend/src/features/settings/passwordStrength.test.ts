import { describe, it, expect } from "vitest";
import { passwordStrength } from "./passwordStrength";

describe("passwordStrength", () => {
    it("returns score 0 and no label for an empty password", () => {
        expect(passwordStrength("")).toEqual({ score: 0, labelKey: null });
    });

    it("rates a short single-class password as weak", () => {
        const { score, labelKey } = passwordStrength("abc");
        expect(score).toBe(1);
        expect(labelKey).toBe("pw_strength_weak");
    });

    it("gains a point for reaching 8 characters", () => {
        expect(passwordStrength("abcdefgh").score).toBe(2);
    });

    it("gains another point at 12 characters", () => {
        expect(passwordStrength("abcdefghijkl").score).toBe(3);
    });

    it("rates a long, varied password as strong", () => {
        const { score, labelKey } = passwordStrength("Abcd1234efgh");
        expect(score).toBe(4);
        expect(labelKey).toBe("pw_strength_strong");
    });

    it("never exceeds a score of 4", () => {
        expect(passwordStrength("Abcd1234!@#$EFGH").score).toBe(4);
    });
});
