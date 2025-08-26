import { UtcIsoString } from "../types/common/DateTypes";

export type DateInput = Date | UtcIsoString | number;

export function isUtcIsoString(value: unknown): value is UtcIsoString {
    return (
        typeof value === "string" &&
        /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?Z$/.test(value)
    );
}

export function parseDate(input: DateInput): Date {
    if (input instanceof Date) return input;
    if (typeof input === "number") return new Date(input);
    const d = new Date(input);
    if (isNaN(d.getTime())) {
        throw new Error(`Invalid date input: ${String(input)}`);
    }
    return d;
}

export type FormatDateOptions = {
    locale?: string;
    timeZone?: 'local' | 'utc';
    dateStyle?: 'full' | 'long' | 'medium' | 'short';
    timeStyle?: 'full' | 'long' | 'medium' | 'short' | 'none';
};

export function formatDate(input: DateInput, opts: FormatDateOptions = {}): string {
    const { locale, timeZone = 'local', dateStyle = 'medium', timeStyle = 'short' } = opts;

    const d = parseDate(input);
    const tz = timeZone === 'utc' ? 'UTC' : undefined;

    if (timeStyle === 'none') {
        return new Intl.DateTimeFormat(locale, { dateStyle, timeZone: tz }).format(d);
    }
    return new Intl.DateTimeFormat(locale, { dateStyle, timeStyle, timeZone: tz }).format(d);
}

export function formatRelative(input: DateInput, locale?: string): string {
    const d = parseDate(input).getTime();
    const now = Date.now();
    const diff = d - now;

    const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' });

    const absDiff = Math.abs(diff);
    const minutes = Math.round(diff / (60_000));
    if (absDiff < 60_000) return rtf.format(Math.round(diff / 1000), 'second');
    if (absDiff < 3_600_000) return rtf.format(minutes, 'minute');

    const hours = Math.round(diff / 3_600_000);
    if (absDiff < 86_400_000) return rtf.format(hours, 'hour');

    const days = Math.round(diff / 86_400_000);
    if (absDiff < 30 * 86_400_000) return rtf.format(days, 'day');

    const months = Math.round(diff / (30 * 86_400_000));
    if (absDiff < 365 * 86_400_000) return rtf.format(months, 'month');

    const years = Math.round(diff / (365 * 86_400_000));
    return rtf.format(years, 'year');
}

export function nowUtcIso(): UtcIsoString {
    return new Date().toISOString();
}

export function toUtcIso(input: DateInput): UtcIsoString {
    return parseDate(input).toISOString();
}
