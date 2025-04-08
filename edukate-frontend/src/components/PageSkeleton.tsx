import { Box } from "@mui/material";
import { ScreenAdaptingTopBar } from "./topbar/ScreenAdaptingTopBar";
import { Outlet } from "react-router-dom";
import { ParticlesComponent } from "./animation/Particles";
import { useAuthContext } from "./auth/AuthContextProvider";
import { PendingStatusSnackbar } from "./snackbars/PendingStatusSnackbar";

export default function PageSkeleton() {
    const { user } = useAuthContext();

    return (
        <Box>
            <PendingStatusSnackbar open={user != undefined && user.status == "PENDING"}/>
            <ScreenAdaptingTopBar/>
            <Box sx={{ paddingY: "2rem" }}>
                <Outlet/>
                <ParticlesComponent/>
            </Box>
        </Box>
    );
}
