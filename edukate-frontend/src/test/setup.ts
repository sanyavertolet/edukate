import "@testing-library/jest-dom";
import { server } from "./server";

// jsdom v29 changed localStorage to be file-backed, requiring --localstorage-file.
// Provide a simple in-memory implementation so ThemeContext.tsx can use it without errors.
const localStorageMock = (() => {
    const store: Record<string, string> = {};
    return {
        getItem: (key: string) => store[key] ?? null,
        setItem: (key: string, value: string) => {
            store[key] = value;
        },
        removeItem: (key: string) => {
            delete store[key];
        },
        clear: () => {
            Object.keys(store).forEach((k) => delete store[k]);
        },
        get length() {
            return Object.keys(store).length;
        },
        key: (index: number) => Object.keys(store)[index] ?? null,
    };
})();

Object.defineProperty(window, "localStorage", { value: localStorageMock });

beforeAll(() => server.listen({ onUnhandledRequest: "error" }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
