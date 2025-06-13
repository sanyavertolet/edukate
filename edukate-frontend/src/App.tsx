import './App.css'
import { RouterProvider } from "react-router-dom";
import { QueryClientProvider } from "@tanstack/react-query";
import { ThemeProvider } from "./components/themes/ThemeContextProvider";
import { AuthProvider } from "./components/auth/AuthContextProvider";
import { CookiesProvider } from "react-cookie";
import { queryClient } from "./http/queryClient";
import { router } from "./Router";
import { CssBaseline } from "@mui/material";
import { DeviceProvider } from "./components/topbar/DeviceContextProvider";
import { NotificationProvider } from "./components/snackbars/NotificationContextProvider";

export default function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <CssBaseline/>
            <ThemeProvider>
                <AuthProvider>
                    <CookiesProvider defaultSetOptions={{ path: '/' }}>
                        <DeviceProvider>
                            <NotificationProvider>
                                <RouterProvider router={router}/>
                            </NotificationProvider>
                        </DeviceProvider>
                    </CookiesProvider>
                </AuthProvider>
            </ThemeProvider>
        </QueryClientProvider>
    );
};
