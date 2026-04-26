import { Box, Typography } from "@mui/material";

export function AppFooter() {
    const buildTime = new Date(__BUILD_TIME__).toLocaleDateString(undefined, {
        day: "2-digit",
        month: "short",
        year: "numeric",
    });

    return (
        <Box
            component="footer"
            sx={{
                position: "fixed",
                bottom: 0,
                left: 0,
                right: 0,
                textAlign: "center",
                py: 1,
                opacity: 0.4,
                pointerEvents: "none",
                color: "text.primary",
            }}
        >
            <Typography variant="caption">build {buildTime}</Typography>
        </Box>
    );
}
