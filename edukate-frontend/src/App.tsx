import './App.css'
import { RouterProvider } from "react-router-dom";
import { QueryClientProvider } from "@tanstack/react-query";
import { ThemeProvider } from "./components/themes/ThemeContextProvider";
import { AuthProvider } from "./components/auth/AuthContextProvider";
import { CookiesProvider } from "react-cookie";
import { queryClient } from "./http/queryClient";
import { router } from "./Router";
import { CssBaseline } from "@mui/material";

export default function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <CssBaseline/>
            <ThemeProvider>
                <AuthProvider>
                    <CookiesProvider defaultSetOptions={{ path: '/' }}>
                        <RouterProvider router={router}/>
                    </CookiesProvider>
                </AuthProvider>
            </ThemeProvider>
        </QueryClientProvider>
    );
};
