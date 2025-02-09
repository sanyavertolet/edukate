import {Box} from "@mui/material";
import TopBar from "./TopBar.tsx";
import {Outlet} from "react-router-dom";

export default function PageSkeleton() {
    return (
        <Box>
            <TopBar/>
            <Outlet/>
        </Box>
    );
}
