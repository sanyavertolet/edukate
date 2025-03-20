import { useNavigate } from "react-router-dom";
import { Box, Button, Typography } from "@mui/material";

const topBarElements = [
    { text: "Problems", href: "/problems" },
];

export const ToolbarLeftComponent = () => {
    const navigate = useNavigate();
    return (
        <Box sx={{ display: { md: 'flex' }, paddingLeft: { md: "1rem" } }}>
            { topBarElements.map(({ text, href }) => (
                <Button key={ text } onClick={ () => navigate(href) }>
                    <Typography variant={"body2"}>
                        { text }
                    </Typography>
                </Button>
            ))}
        </Box>
    );
}
