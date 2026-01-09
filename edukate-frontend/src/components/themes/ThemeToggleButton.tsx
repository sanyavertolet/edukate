import { useTheme } from "./ThemeContextProvider";
import { IconButton, IconButtonProps } from "@mui/material";
import { Brightness4, Brightness7 } from "@mui/icons-material";

export default function ThemeToggleButton(props: IconButtonProps) {
    const { theme, toggleTheme } = useTheme();

    return (
        <IconButton onClick={toggleTheme} color="primary" aria-label="Theme toggle" {...props }>
            {theme === 'dark' ? <Brightness7 /> : <Brightness4 />}
        </IconButton>
    );
}
