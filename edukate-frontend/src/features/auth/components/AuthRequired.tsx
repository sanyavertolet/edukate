import { useAuthContext } from "@/features/auth/context";
import { ReactNode, useState } from "react";
import { Box, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { SignInForm } from "./SignInForm";
import { SignUpForm } from "./SignUpForm";

type AuthRequiredProps = {
    children: ReactNode;
    bypass?: boolean;
};

export function AuthRequired({ children, bypass = false }: AuthRequiredProps) {
    const { isAuthorized } = useAuthContext();
    const { t } = useTranslation("auth");
    const navigate = useNavigate();
    const [isSignUp, setIsSignUp] = useState(false);
    if (isAuthorized || bypass) {
        return <>{children}</>;
    }
    return (
        <Box sx={{ mt: 4 }}>
            <Typography variant={"h5"} align={"center"} color={"primary"}>
                {t("auth_required_title")}
            </Typography>
            <Typography variant={"body1"} color={"secondary"} align={"center"}>
                {t("auth_required_description")}
            </Typography>
            {isSignUp ? (
                <SignUpForm
                    onSignInRequest={() => {
                        setIsSignUp(false);
                    }}
                />
            ) : (
                <SignInForm
                    onSignUpRequest={() => {
                        setIsSignUp(true);
                    }}
                    onForgotPassword={() => {
                        void navigate("/forgot-password");
                    }}
                />
            )}
        </Box>
    );
}
