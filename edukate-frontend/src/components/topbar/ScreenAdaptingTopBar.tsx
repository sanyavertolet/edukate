import {AppBar, Box, Toolbar } from "@mui/material";
import { ToolbarHomeComponent } from "./ToolbarHomeComponent";
import { ToolbarLinksComponent } from "./ToolbarLinksComponent";
import { ToolbarMenuComponent } from "./ToolbarMenuComponent";
import { useDeviceContext } from "./DeviceContextProvider";

export function ScreenAdaptingTopBar(){
    const { isMobile } = useDeviceContext();

    return (
        <AppBar sx={{ 
            position: "static", 
            background: "transparent", 
            backdropFilter: "blur(8px)",
            zIndex: 1000
        }}>
            <Box>
                <Toolbar>
                    <ToolbarHomeComponent/>
                    { !isMobile && <ToolbarLinksComponent/> }
                    <Box sx={{ flexGrow: 1 }}/>
                    <ToolbarMenuComponent/>
                </Toolbar>
            </Box>
        </AppBar>
    );
}
