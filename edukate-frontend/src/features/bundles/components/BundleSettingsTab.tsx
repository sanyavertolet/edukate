import { FC } from "react";
import { Box, Paper, Typography } from "@mui/material";
import { Bundle } from "@/features/bundles/types";
import { BundleUserManagement } from "./BundleUserManagement";

interface BundleSettingsTabProps {
    bundle: Bundle;
}

export const BundleSettingsTab: FC<BundleSettingsTabProps> = ({ bundle }) => {
    return (
        <Box>
            <Paper variant={"outlined"} sx={{ p: 2, m: 2, mx: "auto", width: { xs: "100%", md: "50%" } }}>
                <Box>
                    <Typography color={"primary"} variant={"h4"} align="center">
                        Users
                    </Typography>
                    <BundleUserManagement shareCode={bundle.shareCode} />
                </Box>
            </Paper>
        </Box>
    );
};
