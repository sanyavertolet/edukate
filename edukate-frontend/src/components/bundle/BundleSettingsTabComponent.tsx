import { FC } from "react";
import {Box, Paper, Typography} from "@mui/material";
import { Bundle } from "../../types/bundle/Bundle";
import { UserRolesManagementComponent } from "../user/UserRolesManagementComponent";

interface BundleUserManagementComponentProps {
    bundle: Bundle;
}

const BundleUserManagementComponent: FC<BundleUserManagementComponentProps> = ({ bundle }) => {
    return (
        <Box>
            <Typography color={"primary"} variant={"h4"}>
                Users
            </Typography>
            <UserRolesManagementComponent shareCode={ bundle.shareCode }/>
        </Box>
    )
};

interface BundleSettingsTabComponentProps {
    bundle: Bundle;
}

export const BundleSettingsTabComponent: FC<BundleSettingsTabComponentProps> = ({ bundle }) => {
    return (
        <Box>
            <Paper variant={"outlined"} sx={{ justifySelf: "center", p: 2, m: 2, width: { sm: "100%", md: "50%" } }}>
                <BundleUserManagementComponent bundle={bundle}/>
            </Paper>
        </Box>
    );
};
