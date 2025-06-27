import { FC } from "react";
import { Paper, Typography } from "@mui/material";
import { Bundle } from "../../types/Bundle";

interface BundleSettingsTabComponentProps {
    bundle: Bundle;
}

export const BundleSettingsTabComponent: FC<BundleSettingsTabComponentProps> = ({ bundle }) => {
    return (
        <Paper variant={"outlined"} sx={{ justifySelf: "center", p: 2, m: 2, width: { sm: "100%", md: "50%" } }}>
            <Typography color={"primary"} variant={"h3"}>
                WIP: { bundle.name } settings
            </Typography>
            <Typography variant={"body1"}>
                Later, you will be given a chance to change the bundle data!
            </Typography>
        </Paper>
    );
};
