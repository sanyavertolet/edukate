import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render as rtlRender, RenderOptions } from "@testing-library/react";
import { FC, ReactElement, ReactNode } from "react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { CookiesProvider } from "react-cookie";
import { ThemeProvider } from "@/shared/context/ThemeContext";
import { AuthProvider } from "@/features/auth/context";
import { DeviceProvider } from "@/shared/context/DeviceContext";

function makeQueryClient() {
    return new QueryClient({
        defaultOptions: {
            queries: { retry: false },
            mutations: { retry: false },
        },
    });
}

interface WrapperProps {
    children: ReactNode;
}

export function createWrapper() {
    const queryClient = makeQueryClient();
    const Wrapper: FC<WrapperProps> = ({ children }) => (
        <QueryClientProvider client={queryClient}>
            <ThemeProvider>
                <MemoryRouter>
                    <CookiesProvider>
                        <AuthProvider>
                            <DeviceProvider>{children}</DeviceProvider>
                        </AuthProvider>
                    </CookiesProvider>
                </MemoryRouter>
            </ThemeProvider>
        </QueryClientProvider>
    );
    return Wrapper;
}

function render(ui: ReactElement, options?: Omit<RenderOptions, "wrapper">) {
    return rtlRender(ui, { wrapper: createWrapper(), ...options });
}

/**
 * Renders a page component that uses `useParams` by mounting it inside a
 * `<Route>` so param extraction works.
 *
 * @param path          The URL to seed the router with, e.g. "/problems/my-id"
 * @param routePattern  The route pattern with param tokens, e.g. "/problems/:id"
 * @param ui            The page element to render
 */
function renderAtPath(path: string, routePattern: string, ui: ReactElement) {
    const queryClient = makeQueryClient();
    return rtlRender(
        <QueryClientProvider client={queryClient}>
            <ThemeProvider>
                <MemoryRouter initialEntries={[path]}>
                    <CookiesProvider>
                        <AuthProvider>
                            <DeviceProvider>
                                <Routes>
                                    <Route path={routePattern} element={ui} />
                                </Routes>
                            </DeviceProvider>
                        </AuthProvider>
                    </CookiesProvider>
                </MemoryRouter>
            </ThemeProvider>
        </QueryClientProvider>,
    );
}

export { render, renderAtPath };
export { screen, waitFor, fireEvent, act } from "@testing-library/react";
