import {
    acceptMatches,
    FILE_ERROR_TOO_LARGE,
    FILE_ERROR_TOO_MANY_PIXELS,
    FILE_ERROR_TYPE,
    validateFile,
} from "./fileValidation";

/** Build a minimal valid PNG header (signature + IHDR) declaring the given dimensions. */
function pngFile(width: number, height: number, name = "image.png"): File {
    const bytes = new Uint8Array(24);
    const view = new DataView(bytes.buffer);
    view.setUint32(0, 0x89504e47);
    view.setUint32(4, 0x0d0a1a0a);
    view.setUint32(8, 13); // IHDR chunk length
    bytes.set([0x49, 0x48, 0x44, 0x52], 12); // "IHDR"
    view.setUint32(16, width);
    view.setUint32(20, height);
    return new File([bytes], name, { type: "image/png" });
}

/** Minimal GIF89a header: little-endian logical-screen width/height at offsets 6 and 8. */
function gifFile(width: number, height: number): File {
    const bytes = new Uint8Array(10);
    bytes.set([0x47, 0x49, 0x46, 0x38, 0x39, 0x61], 0); // "GIF89a"
    const view = new DataView(bytes.buffer);
    view.setUint16(6, width, true);
    view.setUint16(8, height, true);
    return new File([bytes], "image.gif", { type: "image/gif" });
}

/** Minimal JPEG with a single SOF0 segment carrying big-endian height/width. */
function jpegFile(width: number, height: number): File {
    const bytes = new Uint8Array(20);
    const view = new DataView(bytes.buffer);
    view.setUint16(0, 0xffd8); // SOI
    view.setUint16(2, 0xffc0); // SOF0 marker
    view.setUint16(4, 0x0011); // segment length
    bytes[6] = 0x08; // sample precision
    view.setUint16(7, height);
    view.setUint16(9, width);
    return new File([bytes], "image.jpg", { type: "image/jpeg" });
}

/** Minimal WEBP/VP8X: 24-bit little-endian (width-1, height-1) at offsets 24 and 27. */
function webpFile(width: number, height: number): File {
    const bytes = new Uint8Array(30);
    const view = new DataView(bytes.buffer);
    view.setUint32(0, 0x52494646); // "RIFF"
    view.setUint32(8, 0x57454250); // "WEBP"
    view.setUint32(12, 0x56503858); // "VP8X"
    const w = width - 1;
    const h = height - 1;
    bytes[24] = w & 0xff;
    bytes[25] = (w >> 8) & 0xff;
    bytes[26] = (w >> 16) & 0xff;
    bytes[27] = h & 0xff;
    bytes[28] = (h >> 8) & 0xff;
    bytes[29] = (h >> 16) & 0xff;
    return new File([bytes], "image.webp", { type: "image/webp" });
}

describe("acceptMatches", () => {
    const png = new File(["x"], "photo.PNG", { type: "image/png" });

    it("matches a MIME wildcard", () => {
        expect(acceptMatches(png, "image/*")).toBe(true);
    });
    it("matches an exact MIME type", () => {
        expect(acceptMatches(png, "image/png")).toBe(true);
    });
    it("matches by extension case-insensitively", () => {
        expect(acceptMatches(png, ".png")).toBe(true);
    });
    it("matches when any token in a comma list matches, ignoring surrounding spaces", () => {
        expect(acceptMatches(png, "application/pdf , image/*")).toBe(true);
    });
    it("accepts everything for the bare and any/any wildcards", () => {
        expect(acceptMatches(png, "*")).toBe(true);
        expect(acceptMatches(png, "*/*")).toBe(true);
    });
    it("rejects a non-matching type", () => {
        expect(acceptMatches(png, "application/pdf")).toBe(false);
    });
    it("rejects an empty accept string (no tokens to match)", () => {
        expect(acceptMatches(png, "")).toBe(false);
    });
    it("does not treat a partial MIME as a wildcard", () => {
        const gif = new File(["x"], "a.gif", { type: "image/gif" });
        expect(acceptMatches(gif, "image/png")).toBe(false);
    });
});

describe("validateFile — rule precedence", () => {
    it("rejects a disallowed type before checking size", async () => {
        const bigPdf = new File([new Uint8Array(4096)], "doc.pdf", { type: "application/pdf" });
        // Wrong type AND over size — the type error must win because it is checked first.
        expect(await validateFile(bigPdf, { accept: "image/*", maxBytes: 1024 })).toBe(FILE_ERROR_TYPE);
    });

    it("rejects a file over the size ceiling", async () => {
        const big = new File([new Uint8Array(2048)], "big.bin");
        expect(await validateFile(big, { maxBytes: 1024 })).toBe(FILE_ERROR_TOO_LARGE);
    });

    it("accepts a file exactly at the size ceiling (boundary is inclusive)", async () => {
        const exact = new File([new Uint8Array(1024)], "exact.bin");
        expect(await validateFile(exact, { maxBytes: 1024 })).toBeNull();
    });
});

describe("validateFile — decode-bomb guard", () => {
    it.each([
        ["png", pngFile(20000, 20000)],
        ["gif", gifFile(20000, 20000)],
        ["jpeg", jpegFile(20000, 20000)],
        ["webp", webpFile(20000, 20000)],
    ])("rejects an oversized %s image (400 MP > 100 MP budget)", async (_format, file) => {
        expect(await validateFile(file, { maxMegapixels: 100 })).toBe(FILE_ERROR_TOO_MANY_PIXELS);
    });

    it.each([
        ["png", pngFile(512, 512)],
        ["gif", gifFile(512, 512)],
        ["jpeg", jpegFile(512, 512)],
        ["webp", webpFile(512, 512)],
    ])("passes a small %s image within budget", async (_format, file) => {
        expect(await validateFile(file, { maxMegapixels: 100 })).toBeNull();
    });

    it("fails open when dimensions cannot be read (unknown/corrupt header)", async () => {
        const fake = new File(["not really an image"], "fake.png", { type: "image/png" });
        expect(await validateFile(fake, { maxMegapixels: 1 })).toBeNull();
    });

    it("skips the pixel check for non-image files", async () => {
        const text = new File([new Uint8Array(10)], "notes.txt", { type: "text/plain" });
        expect(await validateFile(text, { maxMegapixels: 1 })).toBeNull();
    });

    it("skips the pixel check when no megapixel rule is given", async () => {
        expect(await validateFile(pngFile(20000, 20000), { accept: "image/*" })).toBeNull();
    });
});

describe("validateFile — combined", () => {
    it("returns null when every rule passes", async () => {
        const rules = { accept: "image/*", maxBytes: 1024, maxMegapixels: 100 };
        expect(await validateFile(pngFile(256, 256), rules)).toBeNull();
    });

    it("returns null for an empty rule set", async () => {
        expect(await validateFile(new File(["x"], "anything.bin"), {})).toBeNull();
    });
});
