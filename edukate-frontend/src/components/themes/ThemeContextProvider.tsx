import { createContext, FC, ReactNode, useContext, useEffect, useState } from "react";
import { ThemeProvider as MuiThemeProvider } from "@mui/material/styles";
import themes from "./themes";

const THEME_STORAGE_KEY = "edukate-theme";

type Theme = "light" | "dark";

type ThemeContextType = {
    theme: Theme;
    toggleTheme: () => void;
};

const ThemeContext = createContext<ThemeContextType>({
    theme: "light",
    toggleTheme: () => {},
});

interface ThemeProviderProps {
    children: ReactNode;
}

export const ThemeProvider: FC<ThemeProviderProps> = ({children}) => {
    const [selectedTheme, setSelectedTheme] = useState<Theme>(() => {
        return (localStorage.getItem(THEME_STORAGE_KEY) || "light") as Theme;
    });

    useEffect(() => { localStorage.setItem(THEME_STORAGE_KEY, selectedTheme); }, [selectedTheme]);

    const toggleTheme = () => {
        setSelectedTheme((currentTheme) => currentTheme == "light" ? "dark" : "light");
    };

    return (
        <ThemeContext.Provider value={{theme: selectedTheme, toggleTheme: toggleTheme}}>
            <MuiThemeProvider theme={themes[selectedTheme]}>
                { children }
            </MuiThemeProvider>
        </ThemeContext.Provider>
    );
}

export const useTheme = () => useContext(ThemeContext);
