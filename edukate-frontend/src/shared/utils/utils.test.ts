import { formatFileSize, getColorByStringHash, getFirstLetters, sizeOf } from "./utils";

const makeFile = (size: number) => ({ size }) as File;

describe("getFirstLetters", () => {
    it("returns the first n characters of a string", () => {
        expect(getFirstLetters("Alice", 2)).toBe("Al");
    });

    it("returns an empty string when n is 0", () => {
        expect(getFirstLetters("Alice", 0)).toBe("");
    });

    it("returns the whole string when n exceeds its length", () => {
        expect(getFirstLetters("Hi", 10)).toBe("Hi");
    });
});

describe("sizeOf", () => {
    it("returns 0 for an empty array", () => {
        expect(sizeOf([])).toBe(0);
    });

    it("returns the size of a single file", () => {
        expect(sizeOf([makeFile(512)])).toBe(512);
    });

    it("returns the sum of sizes for multiple files", () => {
        expect(sizeOf([makeFile(100), makeFile(200), makeFile(300)])).toBe(600);
    });
});

describe("formatFileSize", () => {
    it('returns "0 Bytes" for 0', () => {
        expect(formatFileSize(0)).toBe("0 Bytes");
    });

    it("formats bytes", () => {
        expect(formatFileSize(500)).toBe("500 Bytes");
    });

    it("formats kilobytes", () => {
        expect(formatFileSize(1024)).toBe("1 KB");
    });

    it("formats fractional kilobytes", () => {
        expect(formatFileSize(1536)).toBe("1.5 KB");
    });

    it("formats megabytes", () => {
        expect(formatFileSize(1024 * 1024)).toBe("1 MB");
    });

    it("formats gigabytes", () => {
        expect(formatFileSize(1024 * 1024 * 1024)).toBe("1 GB");
    });
});

describe("getColorByStringHash", () => {
    it("returns a hex color string", () => {
        expect(getColorByStringHash("alice")).toMatch(/^#[0-9A-F]{6}$/i);
    });

    it("is deterministic — same input always returns the same color", () => {
        expect(getColorByStringHash("alice")).toBe(getColorByStringHash("alice"));
    });

    it("handles an empty string without throwing", () => {
        expect(() => getColorByStringHash("")).not.toThrow();
    });
});
