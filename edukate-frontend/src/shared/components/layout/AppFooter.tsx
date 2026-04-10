import { Box, Typography } from "@mui/material";

export function AppFooter() {
    const version = import.meta.env.VITE_APP_VERSION;
    const commit = __GIT_COMMIT__;

    return (
        <Box component="footer" sx={{ textAlign: "center", py: 2, opacity: 0.4 }}>
            <Typography variant="caption">
                v{version} ({commit})
            </Typography>
        </Box>
    );
}
