import { Box, Button, Menu, MenuItem } from "@mui/material";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthContext } from "./auth/AuthContextProvider";
import { useSignOut } from "../http/auth";
import { AccountCircle } from "@mui/icons-material";

export function UserMenu() {
    const { user } = useAuthContext();
    const navigate = useNavigate();
    const signOut = useSignOut();

    const handleOpen = (event: React.MouseEvent<HTMLElement>) => { setAnchorEl(event.currentTarget) };
    const handleClose = () => { setAnchorEl(undefined) };
    const handleSignOut = () => { signOut(); handleClose() };
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
        <Box alignContent="center" paddingLeft="0.5rem">
            <Menu
                id="edukate-menu"
                anchorEl={ anchorEl }
                anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
                open={ isMenuOpen }
                onClose={ handleClose }
                MenuListProps={{ 'aria-labelledby': 'basic-button' }}
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
                { user ? user.name : "Sign" }
                <AccountCircle sx={{ paddingLeft: "0.5rem" }} />
            </Button>
        </Box>
    );
}
