import { getCookie, removeCookie } from "typescript-cookie";

export const TOKEN_COOKIE = 'edukate-token';
const DEFAULT_COOKIE_MAX_AGE_SECONDS = 29 * 60;

interface CookieOptions {
    path: string;
    domain: string;
    secure: boolean;
    httpOnly: boolean;
    sameSite: "lax" | "strict" | "none";
    partitioned: boolean;
    maxAge?: number;
    expires?: Date;
}

export const defaultCookieOptions: CookieOptions = {
    path: "/",
    domain: window.location.hostname,
    secure: false,
    httpOnly: false,
    sameSite: "lax",
    partitioned: false,
    expires: new Date(Date.now() + DEFAULT_COOKIE_MAX_AGE_SECONDS * 1000)
};

export function removeCookies() {
    removeCookie(TOKEN_COOKIE, defaultCookieOptions);
}

export function getTokenFromCookies() {
    return getCookie(TOKEN_COOKIE);
}
