import { Box, Button, Divider, Menu, MenuItem, Typography } from "@mui/material";
import { MouseEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthContext } from "@/features/auth/context";
import { useSignOutMutation } from "@/features/auth/api";
import { AccountCircle } from "@mui/icons-material";
import { queryClient } from "@/lib/query-client";
import { useTranslation } from "react-i18next";
import { useDeviceContext } from "@/shared/context/DeviceContext";
import { useTheme } from "@/shared/context/ThemeContext";
import { UserAvatar } from "@/shared/components/UserAvatar";

export function UserMenu() {
    const { t, i18n } = useTranslation("navigation");
    const { user } = useAuthContext();
    const navigate = useNavigate();
    const signOutMutation = useSignOutMutation();
    const { isMobile } = useDeviceContext();
    const { theme, toggleTheme } = useTheme();
    const isRussian = i18n.language.startsWith("ru");

    const toggleLanguage = () => {
        void i18n.changeLanguage(isRussian ? "en" : "ru").then(() => {
            void queryClient.invalidateQueries();
        });
    };

    const handleOpen = (event: MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(undefined);
    };
    const handleSignOut = () => {
        signOutMutation.mutate(undefined, {
            onSuccess: () => {
                void queryClient.refetchQueries({ queryKey: ["whoami"] }).finally(() => {
                    handleClose();
                    window.location.reload();
                });
            },
        });
    };
    const handleSignIn = () => {
        void navigate("/sign-in");
        handleClose();
    };
    const handleSignUp = () => {
        void navigate("/sign-up");
        handleClose();
    };

    const signedOutMenuItems = [
        <MenuItem key="sign-in" onClick={handleSignIn}>
            {t("sign_in")}
        </MenuItem>,
        <MenuItem key="sign-up" onClick={handleSignUp}>
            {t("sign_up")}
        </MenuItem>,
    ];

    const signedInMenuItems = [
        <MenuItem key="sign-out" onClick={handleSignOut}>
            {t("sign_out")}
        </MenuItem>,
    ];

    const mobileSettingsItems = isMobile
        ? [
              <Divider key="mobile-divider" sx={{ "&&": { my: 0 } }} />,
              <MenuItem
                  key="toggle-language"
                  onClick={() => {
                      toggleLanguage();
                      handleClose();
                  }}
              >
                  {isRussian ? "English" : "Русский"}
              </MenuItem>,
              <MenuItem
                  key="toggle-theme"
                  onClick={() => {
                      toggleTheme();
                      handleClose();
                  }}
              >
                  {t(theme === "dark" ? "switch_to_light" : "switch_to_dark")}
              </MenuItem>,
          ]
        : [];

    const [anchorEl, setAnchorEl] = useState<HTMLElement>();
    const isMenuOpen = Boolean(anchorEl);
    return (
        <Box alignContent="center">
            <Menu
                id="edukate-menu"
                anchorEl={anchorEl}
                anchorOrigin={{ vertical: "bottom", horizontal: "left" }}
                open={isMenuOpen}
                onClose={handleClose}
                slotProps={{ list: { "aria-labelledby": "user-menu-button" } }}
                keepMounted
            >
                {user && (
                    <Box>
                        <Box sx={{ px: 2, py: 1.5, display: "flex", alignItems: "center", gap: 1.5 }}>
                            <UserAvatar name={user.name} />
                            <Box>
                                <Typography variant="body2" fontWeight="bold">
                                    {user.name}
                                </Typography>
                                {user.email && (
                                    <Typography variant="caption" color="text.secondary" display="block">
                                        {user.email}
                                    </Typography>
                                )}
                            </Box>
                        </Box>
                        <Divider />
                    </Box>
                )}
                {user ? [...signedInMenuItems, ...mobileSettingsItems] : [...signedOutMenuItems, ...mobileSettingsItems]}
            </Menu>
            <Button
                id="user-menu-button"
                aria-label={t("user_menu_aria")}
                aria-haspopup="true"
                aria-expanded={isMenuOpen}
                color="primary"
                onClick={handleOpen}
                sx={{ minWidth: 0, p: 0.5 }}
            >
                {user ? <UserAvatar name={user.name} /> : <AccountCircle />}
            </Button>
        </Box>
    );
}
