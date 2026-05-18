import "@testing-library/jest-dom";
import "@/shared/i18n";
import { server } from "./server";

// IntersectionObserver is not implemented in jsdom — provide a no-op stub
class IntersectionObserverStub {
    observe() {}
    disconnect() {}
    unobserve() {}
}
Object.defineProperty(window, "IntersectionObserver", {
    writable: true,
    configurable: true,
    value: IntersectionObserverStub,
});

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
            Reflect.deleteProperty(store, key);
        },
        clear: () => {
            Object.keys(store).forEach((k) => {
                Reflect.deleteProperty(store, k);
            });
        },
        get length() {
            return Object.keys(store).length;
        },
        key: (index: number) => Object.keys(store)[index] ?? null,
    };
})();

Object.defineProperty(window, "localStorage", { value: localStorageMock });

beforeAll(() => {
    server.listen({ onUnhandledRequest: "error" });
});
afterEach(() => {
    server.resetHandlers();
});
afterAll(() => {
    server.close();
});
