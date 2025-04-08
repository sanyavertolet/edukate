import { ThemeToggleButton } from "../themes/ThemeToggleButton";
import { UserMenu } from "../UserMenu";
import { Box } from "@mui/material";

export const ToolbarMenuComponent = () => {
    return (
        <Box sx={{ display: "flex" }}>
            <ThemeToggleButton/>
            <UserMenu/>
        </Box>
    );
}
