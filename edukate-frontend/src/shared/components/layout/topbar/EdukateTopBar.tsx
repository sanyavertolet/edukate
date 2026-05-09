import Box from "@mui/material/Box";
import AppBar from "@mui/material/AppBar";
import Button from "@mui/material/Button";
import IconButton from "@mui/material/IconButton";
import Container from "@mui/material/Container";
import MenuIcon from "@mui/icons-material/Menu";
import { SiteMark } from "./SiteMark";
import { useLocation, useNavigate } from "react-router-dom";
import { MobileDrawerComponent } from "./MobileDrawer";
import { desktopNavigationElements } from "./NavigationElement";
import { FC, useState } from "react";
import { useAuthContext } from "@/features/auth/context";
import { NotificationButton } from "@/features/notifications/components/NotificationButton";
import ThemeToggleButton from "./ThemeToggleButton";
import { UserMenu } from "./UserMenu";
import { BlurryToolbar } from "@/shared/components/Styled";
import { LanguageSwitcher } from "@/shared/components/LanguageSwitcher";
import { useTranslation } from "react-i18next";

const appBarSx = {
    boxShadow: 0,
    bgcolor: "transparent",
    backgroundImage: "none",
    mt: "calc(var(--template-frame-height, 0px) + 28px)",
} as const;

export function EdukateTopBar() {
    const { t } = useTranslation("navigation");
    const [open, setOpen] = useState(false);

    const toggleDrawer = (newOpen: boolean) => () => {
        setOpen(newOpen);
    };

    const { isAuthorized } = useAuthContext();
    const navigate = useNavigate();
    const { pathname } = useLocation();
    const isSignUpPage = pathname === "/sign-up";
    const isSignInPage = pathname === "/sign-in";
    const isNavActive = (href: string) => pathname === href;
    return (
        <AppBar position="fixed" enableColorOnDark sx={appBarSx}>
            <Container maxWidth="lg">
                <BlurryToolbar variant="regular" disableGutters>
                    <Box sx={{ flexGrow: 1, display: "flex", alignItems: "center", px: 0 }}>
                        <Box sx={{ display: { xs: "flex", md: "none" }, gap: 1 }}>
                            <IconButton aria-label={t("menu_button_aria")} onClick={toggleDrawer(true)}>
                                <MenuIcon />
                            </IconButton>
                            <MobileDrawerComponent isOpen={open} setIsOpen={setOpen} />
                        </Box>
                        <SiteMark
                            onClick={() => {
                                void navigate("/");
                            }}
                        />
                        <Box sx={{ display: { xs: "none", md: "flex" } }}>
                            {desktopNavigationElements.map((el) => (
                                <TopBarLink
                                    key={el.href}
                                    text={t(el.text)}
                                    isActive={isNavActive(el.href)}
                                    onClick={() => {
                                        void navigate(el.href);
                                    }}
                                />
                            ))}
                        </Box>
                    </Box>

                    <Box sx={{ display: "flex", alignItems: "center" }}>
                        <LanguageSwitcher />
                        <ThemeToggleButton />

                        {isAuthorized ? (
                            <Box sx={{ display: "flex", gap: 1, alignItems: "center" }}>
                                <NotificationButton />
                                <UserMenu />
                            </Box>
                        ) : (
                            <Box sx={{ display: { xs: "none", md: "flex" }, gap: 1, alignItems: "center" }}>
                                <TopBarLink
                                    text={t("sign_in")}
                                    onClick={() => {
                                        void navigate("/sign-in", { replace: isSignUpPage || isSignInPage });
                                    }}
                                />
                                <TopBarLink
                                    text={t("sign_up")}
                                    onClick={() => {
                                        void navigate("/sign-up", { replace: isSignUpPage || isSignInPage });
                                    }}
                                />
                            </Box>
                        )}
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
    isActive?: boolean;
};

const TopBarLink: FC<TopBarLinkProps> = ({ text, onClick, variant = "text", disabled = false, isActive = false }) => {
    return (
        <Button
            variant={variant}
            color={isActive ? "secondary" : "primary"}
            size="small"
            onClick={onClick}
            disabled={disabled}
        >
            {text}
        </Button>
    );
};
