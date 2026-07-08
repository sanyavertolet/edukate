/**
 * Shared, framework-agnostic guards for every client-side file pick/upload.
 *
 * `validateFile` returns a *namespaced* i18n key (e.g. `"common:file_too_large"`) on the
 * first failing rule, or `null` when the file passes. Callers render it with `t(key)`
 * regardless of their own default namespace, and decide how to surface it (Alert, toast…).
 * The backend remains the authoritative gate — these checks only stop honest mistakes
 * early and keep the UI from choking on pathological inputs.
 */

// Namespaced so `t(key)` resolves from any component's active namespace. Keys live in common.json.
export const FILE_ERROR_TYPE = "common:file_type_not_accepted";
export const FILE_ERROR_TOO_LARGE = "common:file_too_large";
export const FILE_ERROR_TOO_MANY_PIXELS = "common:file_too_many_pixels";

/** Decode-bomb ceiling: allows every real consumer camera (≤108 MP) while rejecting raster bombs. */
export const DEFAULT_MAX_MEGAPIXELS = 100;

export interface FileValidationRules {
    /** `<input accept>` syntax ("image/*", ".png,.jpg", "application/pdf"). Omit or "*" to allow anything. */
    accept?: string;
    /** Per-file size ceiling, in bytes. */
    maxBytes?: number;
    /** Decode-bomb guard, in megapixels (width × height ÷ 1e6). Only applied to images. */
    maxMegapixels?: number;
}

/**
 * Validate a single file against `rules`. Cheap synchronous checks (type, size) run first;
 * the async pixel check only runs for images that passed them. Resolves to an error key or null.
 */
export async function validateFile(file: File, rules: FileValidationRules): Promise<string | null> {
    if (rules.accept && !acceptMatches(file, rules.accept)) return FILE_ERROR_TYPE;
    if (rules.maxBytes !== undefined && file.size > rules.maxBytes) return FILE_ERROR_TOO_LARGE;

    if (rules.maxMegapixels !== undefined && file.type.startsWith("image/")) {
        const dimensions = await readImageDimensions(file);
        // Fail-open: an unmeasurable file (unknown format, truncated header) is left to the
        // backend rather than rejected here, so we never block a file we simply couldn't read.
        if (dimensions && (dimensions.width * dimensions.height) / 1_000_000 > rules.maxMegapixels) {
            return FILE_ERROR_TOO_MANY_PIXELS;
        }
    }

    return null;
}

/**
 * Mirror the browser's `<input accept>` matching: a file passes if it matches ANY token.
 * Tokens may be a bare or "any/any" wildcard, a MIME wildcard (`image/*`), an exact MIME
 * (`application/pdf`), or an extension (`.png`). Matching is case-insensitive.
 */
export function acceptMatches(file: File, accept: string): boolean {
    const name = file.name.toLowerCase();
    const type = file.type.toLowerCase();
    return accept
        .split(",")
        .map((token) => token.trim().toLowerCase())
        .filter(Boolean)
        .some((token) => {
            if (token === "*" || token === "*/*") return true;
            if (token.startsWith(".")) return name.endsWith(token);
            if (token.endsWith("/*")) return type.startsWith(token.slice(0, -1)); // "image/" prefix
            return type === token;
        });
}

interface Dimensions {
    width: number;
    height: number;
}

/**
 * Read an image's declared pixel dimensions from its header WITHOUT decoding the raster —
 * this is what makes the megapixel guard safe against decode bombs. Only the leading bytes
 * are read; unknown/corrupt formats resolve to null (the caller then skips the pixel check).
 */
async function readImageDimensions(file: File): Promise<Dimensions | null> {
    try {
        // Headers (incl. JPEG SOF after EXIF) live near the start; 128 KB is a generous window.
        const view = new DataView(await file.slice(0, 128 * 1024).arrayBuffer());
        return parsePng(view) ?? parseGif(view) ?? parseWebp(view) ?? parseJpeg(view);
    } catch {
        return null;
    }
}

function parsePng(view: DataView): Dimensions | null {
    // Signature (8 bytes) + IHDR length/type (8) then width/height as big-endian uint32.
    if (view.byteLength < 24) return null;
    if (view.getUint32(0) !== 0x89504e47 || view.getUint32(4) !== 0x0d0a1a0a) return null;
    return { width: view.getUint32(16), height: view.getUint32(20) };
}

function parseGif(view: DataView): Dimensions | null {
    // "GIF8" magic; logical-screen width/height are little-endian uint16 at offsets 6 and 8.
    if (view.byteLength < 10 || view.getUint32(0) !== 0x47494638) return null;
    return { width: view.getUint16(6, true), height: view.getUint16(8, true) };
}

function parseJpeg(view: DataView): Dimensions | null {
    if (view.byteLength < 4 || view.getUint16(0) !== 0xffd8) return null;
    let offset = 2;
    while (offset + 9 < view.byteLength) {
        if (view.getUint8(offset) !== 0xff) {
            offset++; // skip fill bytes / resync
            continue;
        }
        const marker = view.getUint8(offset + 1);
        // Start-Of-Frame markers carry the dimensions; C4/C8/CC are DHT/JPG/DAC, not frames.
        if (marker >= 0xc0 && marker <= 0xcf && marker !== 0xc4 && marker !== 0xc8 && marker !== 0xcc) {
            return { height: view.getUint16(offset + 5), width: view.getUint16(offset + 7) };
        }
        if (marker === 0xd8 || marker === 0xd9) {
            offset += 2; // SOI/EOI have no length
            continue;
        }
        const segmentLength = view.getUint16(offset + 2);
        if (segmentLength < 2) return null;
        offset += 2 + segmentLength;
    }
    return null;
}

function parseWebp(view: DataView): Dimensions | null {
    // "RIFF"...."WEBP"; three sub-formats each store canvas dimensions differently.
    if (view.byteLength < 30 || view.getUint32(0) !== 0x52494646 || view.getUint32(8) !== 0x57454250) return null;
    const format = view.getUint32(12);
    if (format === 0x56503858) {
        // "VP8X": 24-bit little-endian (width-1, height-1) at offsets 24 and 27.
        const width = 1 + (view.getUint8(24) | (view.getUint8(25) << 8) | (view.getUint8(26) << 16));
        const height = 1 + (view.getUint8(27) | (view.getUint8(28) << 8) | (view.getUint8(29) << 16));
        return { width, height };
    }
    if (format === 0x56503820) {
        // "VP8 " (lossy): 14-bit dimensions in the keyframe header at offsets 26/28.
        return { width: view.getUint16(26, true) & 0x3fff, height: view.getUint16(28, true) & 0x3fff };
    }
    if (format === 0x5650384c) {
        // "VP8L" (lossless): 14-bit (width-1, height-1) packed after the 0x2f signature byte.
        const bits = view.getUint32(21, true);
        return { width: 1 + (bits & 0x3fff), height: 1 + ((bits >> 14) & 0x3fff) };
    }
    return null;
}
