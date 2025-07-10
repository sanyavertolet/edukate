import { Box } from "@mui/material";
import { ScreenAdaptingTopBar } from "./topbar/ScreenAdaptingTopBar";
import { Outlet } from "react-router-dom";
import { ParticlesComponent } from "./animation/Particles";
import { useAuthContext } from "./auth/AuthContextProvider";
import { useEffect } from "react";
import { toast } from "react-toastify";

export default function PageSkeleton() {
    const { user } = useAuthContext();

    useEffect(
        () => {
            if (user?.status === "PENDING") {
                toast.info("Your account is pending approval. Some features are temporarily restricted.");
            }
        },
        // eslint-disable-next-line
        []
    );

    return (
        <Box>
            <ScreenAdaptingTopBar/>
            <Box sx={{ paddingY: "2rem" }}>
                <Outlet/>
                <ParticlesComponent/>
            </Box>
        </Box>
    );
}
