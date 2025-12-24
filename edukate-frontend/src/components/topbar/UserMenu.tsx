import { Box, Button, Menu, MenuItem, Typography } from "@mui/material";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthContext } from "../auth/AuthContextProvider";
import { useSignOutMutation } from "../../http/requests/auth";
import { AccountCircle } from "@mui/icons-material";
import { queryClient } from "../../http/queryClient";

export function UserMenu() {
    const { user } = useAuthContext();
    const navigate = useNavigate();
    const signOutMutation = useSignOutMutation();

    const handleOpen = (event: React.MouseEvent<HTMLElement>) => { setAnchorEl(event.currentTarget) };
    const handleClose = () => { setAnchorEl(undefined) };
    const handleSignOut = () => {
        signOutMutation.mutate(undefined, {
            onSuccess: () => queryClient
                .refetchQueries({ queryKey: ['whoami'] })
                .finally(() => {
                    handleClose();
                    window.location.reload();
                }),
        });
    };
    const handleSignIn = () => { navigate("/sign-in"); handleClose() };
    const handleSignUp = () => { navigate("/sign-up"); handleClose() };

    const signedOutMenuItems = [
        <MenuItem key="sign-in" onClick={ handleSignIn }>Sign In</MenuItem>,
        <MenuItem key="sign-up" onClick={ handleSignUp }>Sign Up</MenuItem>,
    ];

    const signedInMenuItems = [
        <MenuItem key="sign-out" onClick={ handleSignOut }>Sign Out</MenuItem>,
    ];

    const [anchorEl, setAnchorEl] = useState<HTMLElement>();
    const isMenuOpen = Boolean(anchorEl);
    return (
        <Box alignContent="center">
            <Menu id="edukate-menu" anchorEl={ anchorEl } anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
                  open={ isMenuOpen } onClose={ handleClose } MenuListProps={{ 'aria-labelledby': 'basic-button' }}
                  keepMounted
            >
                { user ? signedInMenuItems : signedOutMenuItems }
            </Menu>
            <Button
                aria-label="account of current user"
                aria-haspopup="true"
                color="primary"
                onClick={ handleOpen }
            >
                <Typography variant={"body2"} sx={{ display: { xs: "none", md: "flex" }, pr: "0.5rem"}}>
                    { user ? user.name : "Sign" }
                </Typography>
                <AccountCircle/>
            </Button>
        </Box>
    );
}
