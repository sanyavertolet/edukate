import { useNavigate } from "react-router-dom";
import { Box, Button } from "@mui/material";

const topBarElements = [
    { text: "Problems", href: "/problems" },
];

export const ToolbarLeftComponent = () => {
    const navigate = useNavigate();
    return (
        <Box sx={{ display: { xs: 'flex', md: 'flex' }, paddingLeft: "2rem" }}>
            { topBarElements.map(({ text, href }) => (
                <Button key={ text } onClick={ () => navigate(href) }>
                    { text }
                </Button>
            ))}
        </Box>
    );
}
