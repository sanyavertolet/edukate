import { useTheme } from "./ThemeContextProvider";
import { IconButton } from "@mui/material";
import { Brightness4, Brightness7 } from "@mui/icons-material";

export const ThemeToggleButton = () => {
    const { theme, toggleTheme } = useTheme();

    return (
        <IconButton aria-label="theme toggle" color="primary" onClick={ toggleTheme }>
            { theme == "dark" ? <Brightness7/> : <Brightness4/> }
        </IconButton>
    );
}
