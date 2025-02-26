import { AppBar, Avatar, Box, Button, Toolbar, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

export default function TopBar() {
    const navigate = useNavigate();

    const topBarElements = [
        { text: "Problems", href: "/problems" }
    ];

    const onHomeClick=() => navigate("/");

    return (
        <AppBar sx={{
            position: "static",
            background: "transparent",
            backdropFilter: "blur(8px)",
        }}>
            <Box maxWidth={"xl"}>
                <Toolbar>
                    <Button color={"primary"} onClick={onHomeClick}>
                        <Avatar alt={"Home"} src={"logo.png"} sx={{ mr: 2 }} />
                        <Typography>
                            Edukate
                        </Typography>
                    </Button>

                    <Box sx={{ flexGrow: 1, display: { xs: 'flex', md: 'flex' }, paddingLeft: "2rem" }}>
                        {topBarElements.map(({ text, href }) => (
                            <Button key={text} onClick={() => navigate(href)}>
                                {text}
                            </Button>
                        ))}
                    </Box>
                </Toolbar>
            </Box>
        </AppBar>
    )
}
