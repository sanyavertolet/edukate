import { AppBar, Avatar, Box, Button, Container, Link, Toolbar } from "@mui/material";
import { useNavigate } from "react-router-dom";

export default function TopBar() {
    const navigate = useNavigate();

    const topBarElements = [
        { text: "Problems", href: "/problems" }
    ];

    const buttonStyles = { my: 2, color: 'white', display: 'block' };

    return (
        <AppBar position={"static"}>
            <Container maxWidth={"xl"}>
                <Toolbar>
                    <Link href={"/"}>
                        <Avatar alt={"Home"} src={"../../public/logo.png"} sx={{ mr: 2 }} />
                    </Link>

                    <Box sx={{ flexGrow: 1, display: { xs: 'none', md: 'flex' } }}>
                        {topBarElements.map(({ text, href }) => (
                            <Button key={text} onClick={() => navigate(href)} sx={buttonStyles}>
                                {text}
                            </Button>
                        ))}
                    </Box>
                </Toolbar>
            </Container>
        </AppBar>
    )
}
