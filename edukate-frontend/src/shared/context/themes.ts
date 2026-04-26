import { createTheme } from "@mui/material/styles";

const typography = {
    fontFamily: [
        "-apple-system",
        "BlinkMacSystemFont",
        '"Segoe UI"',
        "Roboto",
        '"Helvetica Neue"',
        "Arial",
        "sans-serif",
    ].join(","),
};

const components = {
    MuiLinearProgress: {
        styleOverrides: {
            root: {
                height: 6,
                borderRadius: 3,
            },
        },
    },
};

const lightTheme = createTheme({
    palette: {
        mode: "light",
        primary: {
            main: "#851691",
        },
        secondary: {
            main: "#007E8A",
        },
        background: {
            default: "#f9ebd9",
            paper: "#f3f1ee",
        },
    },
    typography,
    components,
});

const darkTheme = createTheme({
    palette: {
        mode: "dark",
        primary: {
            main: "#9fa8da",
        },
        secondary: {
            main: "#80deea",
        },
        background: {
            default: "#36393e",
            paper: "#424549",
        },
    },
    typography,
    components,
});

export default {
    light: lightTheme,
    dark: darkTheme,
};

export { lightTheme, darkTheme };
