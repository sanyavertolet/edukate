import { ThemeToggleButton } from "../themes/ThemeToggleButton";
import { UserMenu } from "./UserMenu";
import { Box } from "@mui/material";
import { NotificationButton } from "../notifications/NotificationButton";

export const ToolbarMenuComponent = () => {
    return (
        <Box sx={{ display: "flex", alignItems: "center" }}>
            <NotificationButton/>
            <ThemeToggleButton/>
            <UserMenu/>
        </Box>
    );
}
