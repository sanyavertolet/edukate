import { useNavigate } from "react-router-dom";
import { Box, Button, Typography } from "@mui/material";
import { desktopNavigationElements } from "./NavigationElement";

export const ToolbarLinksComponent = () => {
    const navigate = useNavigate();
    return (
        <Box sx={{ display: { sm: 'none', md: 'flex' }, paddingLeft: { md: "1rem" } }}>
            { desktopNavigationElements.map(({ text, href }) => (
                <Button key={ text } onClick={ () => navigate(href) }>
                    <Typography variant={"body2"}>{ text }</Typography>
                </Button>
            ))}
        </Box>
    );
}
