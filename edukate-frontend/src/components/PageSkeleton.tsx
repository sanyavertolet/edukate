import { Container } from "@mui/material";
import { TopBar } from "./topbar/TopBar";
import { Outlet } from "react-router-dom";
import { ParticlesComponent } from "./animation/Particles";

export default function PageSkeleton() {
    return (
        <Container>
            <TopBar/>
            <Container>
                <Outlet/>
                <ParticlesComponent/>
            </Container>
        </Container>
    );
}
