import { useAuthContext } from "@/features/auth/context";
import { ReactNode, useState } from "react";
import { Box, Container, Typography } from "@mui/material";
import { SignInForm } from "./SignInForm";
import { SignUpForm } from "./SignUpForm";

type AuthRequiredProps = {
    children: ReactNode;
    bypass?: boolean;
};

export function AuthRequired({ children, bypass = false }: AuthRequiredProps) {
    const { isAuthorized } = useAuthContext();
    const [isSignUp, setIsSignUp] = useState(false);
    if (isAuthorized || bypass) {
        return <>{children}</>;
    }
    return (
        <Container maxWidth="md" sx={{ mt: 4 }}>
            <Typography variant={"h5"} align={"center"} color={"primary"}>
                Authentication Required
            </Typography>
            <Typography variant={"body1"} color={"secondary"} align={"center"}>
                You need to sign in to fully access this page.
            </Typography>
            <Box sx={{ width: "100%" }}>
                {isSignUp ? (
                    <SignUpForm onSignInRequest={() => { setIsSignUp(false); }} />
                ) : (
                    <SignInForm onSignUpRequest={() => { setIsSignUp(true); }} />
                )}
            </Box>
        </Container>
    );
}
