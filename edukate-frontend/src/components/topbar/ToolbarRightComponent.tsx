import { ThemeToggleButton } from "../themes/ThemeToggleButton";
import { UserMenu } from "../UserMenu";
import { Box } from "@mui/material";

export const ToolbarRightComponent = () => {
    return (
        <Box sx={{ display: { xs: "flex", md: "flex" }}}>
            <ThemeToggleButton/>
            <UserMenu/>
        </Box>
    );
}
