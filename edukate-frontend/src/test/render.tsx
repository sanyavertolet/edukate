import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render as rtlRender, RenderOptions } from "@testing-library/react";
import { FC, ReactElement, ReactNode } from "react";
import { MemoryRouter } from "react-router-dom";
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

export { render };
export { screen, waitFor, fireEvent, act } from "@testing-library/react";
