import { Bundle } from "../../types/bundle/Bundle";
import { FC } from "react";
import { Box, Divider, Paper, Stack, Typography } from "@mui/material";

interface BundleDescriptionTabComponentProps {
    bundle: Bundle;
}

export const BundleDescriptionTabComponent: FC<BundleDescriptionTabComponentProps> = ({ bundle }) => {
    return (
        <Paper variant={"outlined"} sx={{ justifySelf: "center", p: 2, m: 2, width: { sm: "100%", md: "50%" } }}>
            <Stack spacing={3} useFlexGap>
                <Typography color={"primary"} variant={"h4"}>{bundle.name}</Typography>
                <Box>
                    <Stack direction={"row"} alignItems={"end"} justifyContent={"space-between"}>
                        <Typography textAlign={"start"}>
                            Description: { bundle.description }
                        </Typography>
                    </Stack>
                    <Divider/>
                </Box>
            </Stack>
        </Paper>
    );
}
