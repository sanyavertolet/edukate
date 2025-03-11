import { Container } from "@mui/material";
import { TopBar } from "./topbar/TopBar";
import { Outlet } from "react-router-dom";
import { ParticlesComponent } from "./animation/Particles";
import { useAuthContext } from "./auth/AuthContextProvider";
import { PendingStatusSnackbar } from "./snackbars/PendingStatusSnackbar.tsx";

export default function PageSkeleton() {
    const { user } = useAuthContext();

    return (
        <Container>
            <PendingStatusSnackbar open={user != undefined && user.status == "PENDING"}/>
            <TopBar/>
            <Container>
                <Outlet/>
                <ParticlesComponent/>
            </Container>
        </Container>
    );
}
