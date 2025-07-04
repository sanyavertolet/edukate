import { ThemeToggleButton } from "../themes/ThemeToggleButton";
import { UserMenu } from "./UserMenu";
import { Box } from "@mui/material";
import { NotificationButton } from "../notifications/NotificationButton.tsx";

export const ToolbarMenuComponent = () => {
    return (
        <Box sx={{ display: "flex"}}>
            <NotificationButton/>
            <ThemeToggleButton/>
            <UserMenu/>
        </Box>
    );
}
