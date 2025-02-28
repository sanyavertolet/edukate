import { createContext, FC, ReactNode, useContext, useEffect, useState } from "react";
import themes from "./themes"
import { ThemeProvider as MuiThemeProvider } from "@mui/material/styles";

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
        return (localStorage.getItem("edukate-theme") || "light") as Theme;
    });

    useEffect(() => { localStorage.setItem('edukate-theme', selectedTheme); }, [selectedTheme]);

    const toggleTheme = () => {
        setSelectedTheme((currentTheme) => {
            if (currentTheme == "light") {
                return "dark";
            } else {
                return "light";
            }
        })
    };

    return (
        <ThemeContext.Provider value={{theme: selectedTheme, toggleTheme: toggleTheme}}>
            <MuiThemeProvider theme={themes[selectedTheme]}>
                {children}
            </MuiThemeProvider>
        </ThemeContext.Provider>
    )
}

export const useTheme = () => useContext(ThemeContext);
