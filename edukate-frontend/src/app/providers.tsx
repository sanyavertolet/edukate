import { ReactNode } from "react";
import { QueryClientProvider } from "@tanstack/react-query";
import { ThemeProvider } from "@/shared/context/ThemeContext";
import { AuthProvider } from "@/features/auth/context";
import { CookiesProvider } from "react-cookie";
import { queryClient } from "@/lib/query-client";
import { CssBaseline } from "@mui/material";
import { DeviceProvider } from "@/shared/context/DeviceContext";

interface ProvidersProps {
    children: ReactNode;
}

export function Providers({ children }: ProvidersProps) {
    return (
        <QueryClientProvider client={queryClient}>
            <CssBaseline />
            <ThemeProvider>
                <AuthProvider>
                    <CookiesProvider defaultSetOptions={{ path: "/" }}>
                        <DeviceProvider>{children}</DeviceProvider>
                    </CookiesProvider>
                </AuthProvider>
            </ThemeProvider>
        </QueryClientProvider>
    );
}
