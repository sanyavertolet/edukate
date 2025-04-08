import { Avatar, Box, Button, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { MobileDrawerComponent } from "./MobileDrawerComponent";
import { useDeviceContext } from "./DeviceContextProvider";

export const ToolbarHomeComponent = () => {
    const navigate = useNavigate();
    const { isMobile } = useDeviceContext();

    const [isOpen, setIsOpen] = useState(false);
    if (!isMobile) {
        return (
            <Button color="primary" onClick={() => navigate("/")}>
                <Avatar alt="Home" src="logo.png"/>
                <Typography sx={{display: {xs: "none", md: "flex"}, ml: 2}}>
                    Edukate
                </Typography>
            </Button>
        );
    }

    return (
        <Box>
            <Button color="primary" onClick={() => setIsOpen(true)}>
                <Avatar alt="Home" src="logo.png"/>
            </Button>
            <MobileDrawerComponent isOpen={isOpen} setIsOpen={setIsOpen}/>
        </Box>
    )
}
