import { useAuthContext } from "./AuthContextProvider";
import { ReactNode, useState } from "react";
import { Box, Container, Typography } from "@mui/material";
import { SignInComponent } from "./SignInComponent";
import { SignUpComponent } from "./SignUpComponent";

type AuthRequiredProps = {
    children: ReactNode;
    bypass?: boolean;
};

export function AuthRequired({children, bypass = false}: AuthRequiredProps) {
    const { isAuthorized } = useAuthContext();
    const [isSignUp, setIsSignUp] = useState(false);
    if (isAuthorized || bypass) { return <>{children}</>; }
    return (
        <Container maxWidth="md" sx={{mt: 4}}>
                <Typography variant={"h5"} align={"center"} color={"primary"}>
                    Authentication Required
                </Typography>
                <Typography variant={"body1"} color={"secondary"} align={"center"} >
                    You need to sign in to fully access this page.
                </Typography>
                <Box sx={{width: "100%"}}>
                    { isSignUp
                        ? <SignUpComponent onSignInRequest={() => setIsSignUp(false)}/>
                        : <SignInComponent onSignUpRequest={() => setIsSignUp(true)}/>
                    }
                </Box>
        </Container>
    )
}
