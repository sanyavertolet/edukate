import { Bundle } from "@/features/bundles/types";
import { FC } from "react";
import { Box, Divider, Paper, Stack, Typography } from "@mui/material";

interface BundleDescriptionTabProps {
    bundle: Bundle;
}

export const BundleDescriptionTab: FC<BundleDescriptionTabProps> = ({ bundle }) => {
    return (
        <Paper variant={"outlined"} sx={{ p: 2, m: 2, mx: "auto", width: { xs: "100%", md: "50%" } }}>
            <Stack spacing={3} useFlexGap>
                <Typography color={"primary"} variant={"h4"} align="center">
                    {bundle.name}
                </Typography>
                <Box>
                    <Stack direction={"row"} alignItems={"end"} justifyContent={"space-between"}>
                        <Typography textAlign={"start"}>Description: {bundle.description}</Typography>
                    </Stack>
                    <Divider />
                </Box>
            </Stack>
        </Paper>
    );
};
