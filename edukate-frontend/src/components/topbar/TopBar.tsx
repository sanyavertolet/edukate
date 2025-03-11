import { AppBar, Box, Toolbar } from "@mui/material";
import { ToolbarHomeComponent } from "./ToolbarHomeComponent";
import { ToolbarLeftComponent } from "./ToolbarLeftComponent";
import { ToolbarRightComponent } from "./ToolbarRightComponent";

export function TopBar(){
    return (
        <AppBar sx={{ position: "static", background: "transparent", backdropFilter: "blur(8px)" }}>
            <Box maxWidth="xl">
                <Toolbar>
                    <ToolbarHomeComponent/>
                    <ToolbarLeftComponent/>
                    <Box sx={{ flexGrow: 1 }}/>
                    <ToolbarRightComponent/>
                </Toolbar>
            </Box>
        </AppBar>
    );
}
