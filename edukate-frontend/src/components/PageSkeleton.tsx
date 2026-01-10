import { Box, Container } from "@mui/material";
import { EdukateTopBar } from "./topbar/EdukateTopBar";
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
        [user?.status]
    );

    return (
        <Box>
            <EdukateTopBar/>
            <Container maxWidth={"lg"} sx={{ pt: "120px", pb: "2rem" }}>
                <Outlet/>
                <ParticlesComponent/>
            </Container>
        </Box>
    );
}
