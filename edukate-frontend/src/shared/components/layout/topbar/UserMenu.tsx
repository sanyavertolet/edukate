import { Box, Button, Menu, MenuItem, Typography } from "@mui/material";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthContext } from "@/features/auth/context";
import { useSignOutMutation } from "@/features/auth/api";
import { AccountCircle } from "@mui/icons-material";
import { queryClient } from "@/lib/query-client";
import { useTranslation } from "react-i18next";

export function UserMenu() {
    const { t } = useTranslation("navigation");
    const { user } = useAuthContext();
    const navigate = useNavigate();
    const signOutMutation = useSignOutMutation();

    const handleOpen = (event: React.MouseEvent<HTMLElement>) => {
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
                {user ? signedInMenuItems : signedOutMenuItems}
            </Menu>
            <Button
                id="user-menu-button"
                aria-label={t("user_menu_aria")}
                aria-haspopup="true"
                aria-expanded={isMenuOpen}
                color="primary"
                onClick={handleOpen}
            >
                <Typography variant={"body2"} sx={{ display: { xs: "none", md: "flex" }, pr: "0.5rem" }}>
                    {user ? user.name : "Sign"}
                </Typography>
                <AccountCircle />
            </Button>
        </Box>
    );
}
