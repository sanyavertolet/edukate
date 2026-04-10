import { formatDate, formatRelative, isUtcIsoString, nowUtcIso, parseDate, toUtcIso } from "./date";

const FIXED_DATE = new Date("2024-06-15T12:30:00Z");
const FIXED_TS = FIXED_DATE.getTime();

// ─── isUtcIsoString ────────────────────────────────────────────────────────────

describe("isUtcIsoString", () => {
    it("returns true for a valid UTC ISO string", () => {
        expect(isUtcIsoString("2024-06-15T12:30:00Z")).toBe(true);
    });

    it("returns true for a UTC ISO string with milliseconds", () => {
        expect(isUtcIsoString("2024-06-15T12:30:00.123Z")).toBe(true);
    });

    it("returns false for a string without the trailing Z", () => {
        expect(isUtcIsoString("2024-06-15T12:30:00")).toBe(false);
    });

    it("returns false for a plain date string", () => {
        expect(isUtcIsoString("2024-06-15")).toBe(false);
    });

    it("returns false for a number", () => {
        expect(isUtcIsoString(FIXED_TS)).toBe(false);
    });

    it("returns false for null", () => {
        expect(isUtcIsoString(null)).toBe(false);
    });
});

// ─── parseDate ────────────────────────────────────────────────────────────────

describe("parseDate", () => {
    it("returns a Date object unchanged", () => {
        expect(parseDate(FIXED_DATE)).toBe(FIXED_DATE);
    });

    it("parses a Unix timestamp (number)", () => {
        expect(parseDate(FIXED_TS)).toEqual(FIXED_DATE);
    });

    it("parses a valid UTC ISO string", () => {
        expect(parseDate("2024-06-15T12:30:00Z")).toEqual(FIXED_DATE);
    });

    it("throws for an invalid date string", () => {
        expect(() => parseDate("not-a-date")).toThrow("Invalid date input");
    });
});

// ─── formatDate ───────────────────────────────────────────────────────────────

describe("formatDate", () => {
    it("includes the year in the output", () => {
        expect(formatDate(FIXED_DATE, { locale: "en-US", timeZone: "utc" })).toContain("2024");
    });

    it("omits the time when timeStyle is 'none'", () => {
        const result = formatDate(FIXED_DATE, { locale: "en-US", timeZone: "utc", timeStyle: "none" });
        expect(result).not.toContain("12:30");
        expect(result).not.toContain("PM");
    });

    it("includes the time when timeStyle is set", () => {
        const result = formatDate(FIXED_DATE, { locale: "en-US", timeZone: "utc", timeStyle: "short" });
        expect(result).toContain("12:30");
    });

    it("accepts a number timestamp as input", () => {
        expect(formatDate(FIXED_TS, { locale: "en-US", timeZone: "utc" })).toContain("2024");
    });

    it("formats with local timezone without throwing", () => {
        expect(formatDate(FIXED_DATE, { locale: "en-US", timeZone: "local" })).toContain("2024");
    });
});

// ─── formatRelative ───────────────────────────────────────────────────────────

describe("formatRelative", () => {
    beforeEach(() => {
        vi.useFakeTimers();
        vi.setSystemTime(FIXED_DATE);
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it("formats a time 30 seconds in the past", () => {
        const past = new Date(FIXED_TS - 30_000);
        expect(formatRelative(past, "en")).toMatch(/second/);
    });

    it("formats a time 5 minutes in the past", () => {
        const past = new Date(FIXED_TS - 5 * 60_000);
        expect(formatRelative(past, "en")).toMatch(/minute/);
    });

    it("formats a time 3 hours in the past", () => {
        const past = new Date(FIXED_TS - 3 * 3_600_000);
        expect(formatRelative(past, "en")).toMatch(/hour/);
    });

    it("formats a time 4 days in the past", () => {
        const past = new Date(FIXED_TS - 4 * 86_400_000);
        expect(formatRelative(past, "en")).toMatch(/day/);
    });

    it("formats a time 2 months in the past", () => {
        const past = new Date(FIXED_TS - 60 * 86_400_000);
        expect(formatRelative(past, "en")).toMatch(/month/);
    });

    it("formats a time 2 years in the past", () => {
        const past = new Date(FIXED_TS - 2 * 365 * 86_400_000);
        expect(formatRelative(past, "en")).toMatch(/year/);
    });

    it("formats a time in the future", () => {
        const future = new Date(FIXED_TS + 10 * 60_000);
        expect(formatRelative(future, "en")).toMatch(/minute/);
    });
});

// ─── nowUtcIso ────────────────────────────────────────────────────────────────

describe("nowUtcIso", () => {
    it("returns a UTC ISO string", () => {
        expect(isUtcIsoString(nowUtcIso())).toBe(true);
    });

    it("reflects the current time", () => {
        vi.useFakeTimers();
        vi.setSystemTime(FIXED_DATE);
        expect(nowUtcIso()).toBe(FIXED_DATE.toISOString());
        vi.useRealTimers();
    });
});

// ─── toUtcIso ─────────────────────────────────────────────────────────────────

describe("toUtcIso", () => {
    it("converts a Date to an ISO string", () => {
        expect(toUtcIso(FIXED_DATE)).toBe("2024-06-15T12:30:00.000Z");
    });

    it("converts a timestamp number to an ISO string", () => {
        expect(toUtcIso(FIXED_TS)).toBe("2024-06-15T12:30:00.000Z");
    });

    it("converts a UTC ISO string to a canonical ISO string", () => {
        expect(toUtcIso("2024-06-15T12:30:00Z")).toBe("2024-06-15T12:30:00.000Z");
    });
});
