import { useAuthContext } from "./AuthContextProvider";
import { ReactNode, useState } from "react";
import { Box, Link, Paper, Typography } from "@mui/material";
import { SignInComponent } from "./SignInComponent";
import { SignUpComponent } from "./SignUpComponent";

type AuthRequiredProps = {
    children: ReactNode;
};

export function AuthRequired({children}: AuthRequiredProps) {
    const { user } = useAuthContext();
    if (user) { return <>{children}</>; }
    const [isSignUp, setIsSignUp] = useState(false);
    return (
        <Box sx={{
            display: "flex",
            justifyContent: "center",
            p: 3
        }}>
            <Paper elevation={3} sx={{maxWidth: "500px", p: 3, display: "flex", flexDirection: "column", gap: 2}}>
                <Typography variant={"h5"} align={"center"}>Authentication Required</Typography>
                <Typography variant={"body1"}>You need to log in to access this page.</Typography>
                { isSignUp ?
                    <SignUpComponent shouldRefreshInsteadOfNavigate/> :
                    <SignInComponent shouldRefreshInsteadOfNavigate/>}
                { isSignUp ?
                    <Link sx={{ cursor: "pointer" }} onClick={() => setIsSignUp(false)} color={"secondary"}>
                        I already have an account.
                    </Link> : <Link sx={{ cursor: "pointer" }} onClick={() => setIsSignUp(true)} color={"secondary"}>
                        I don't have an account.
                    </Link>}
            </Paper>
        </Box>
    )
}
