import Box from "@mui/material/Box";
import AppBar from "@mui/material/AppBar";
import Button from "@mui/material/Button";
import IconButton from "@mui/material/IconButton";
import Container from "@mui/material/Container";
import MenuIcon from "@mui/icons-material/Menu";
import { SiteMark } from "./SiteMark";
import { useLocation, useNavigate } from "react-router-dom";
import { MobileDrawerComponent } from "./MobileDrawerComponent";
import { FC, useState } from "react";
import { useAuthContext } from "../auth/AuthContextProvider";
import { NotificationButton } from "../notifications/NotificationButton";
import ThemeToggleButton from "../themes/ThemeToggleButton";
import { UserMenu } from "./UserMenu";
import { BlurryToolbar } from "../Styled";

export function EdukateTopBar() {
    const [open, setOpen] = useState(false);

    const toggleDrawer = (newOpen: boolean) => () => { setOpen(newOpen); };

    const appBarSx = {
        boxShadow: 0,
        bgcolor: "transparent",
        backgroundImage: "none",
        mt: "calc(var(--template-frame-height, 0px) + 28px)",
    };

    const { isAuthorized } = useAuthContext();
    const navigate = useNavigate();
    const location = useLocation();
    const isSignUpPage = location.pathname === "/sign-up";
    const isSignInPage = location.pathname === "/sign-in";
    return (
        <AppBar position="fixed" enableColorOnDark sx={appBarSx}>
            <Container maxWidth="lg">
                <BlurryToolbar variant="regular" disableGutters>
                    <Box sx={{ flexGrow: 1, display: "flex", alignItems: "center", px: 0 }}>
                        <Box sx={{ display: { xs: "flex", md: "none" }, gap: 1 }}>
                            <IconButton aria-label="Menu button" onClick={toggleDrawer(true)}>
                                <MenuIcon />
                            </IconButton>
                            <MobileDrawerComponent isOpen={open} setIsOpen={setOpen}/>
                        </Box>
                        <SiteMark onClick={() => navigate("/")}/>
                        <Box sx={{ display: { xs: "none", md: "flex" } }}>
                            <TopBarLink text="Problems" onClick={() => navigate("/problems")} />
                            <TopBarLink text="Bundles" onClick={() => navigate("/bundles")} />
                        </Box>
                    </Box>

                    <Box sx={{ display: "flex", alignItems: "center" }}>
                        <ThemeToggleButton />

                        {isAuthorized
                            ? (<Box sx={{ display: "flex", gap: 1, alignItems: "center"}}>
                                <NotificationButton/>
                                <UserMenu/>
                            </Box>)
                            : (<Box sx={{display: "flex", gap: 1, alignItems: "center"}}>
                                <TopBarLink
                                    text={"Sign In"}
                                    onClick={() => navigate("/sign-in", {replace: isSignUpPage || isSignInPage})}/>
                                <TopBarLink
                                    text={"Sign Up"}
                                    onClick={() => navigate("/sign-up", {replace: isSignUpPage || isSignInPage})} />
                            </Box>)
                        }
                    </Box>
                </BlurryToolbar>
            </Container>
        </AppBar>
    );
}

type TopBarLinkProps = {
    text: string;
    onClick: () => void;
    disabled?: boolean;
    variant?: "text" | "contained";
};

const TopBarLink: FC<TopBarLinkProps> = ({text, onClick, variant = "text", disabled = false}) => {
    return (
        <Button variant={variant} color="primary" size="small" onClick={onClick} disabled={disabled}>
            {text}
        </Button>
    );
}
