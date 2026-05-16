import { useEffect, useRef } from "react";
import { Container } from "@mui/material";
import { SignInForm } from "@/features/auth/components/SignInForm";
import { useNavigate, useSearchParams } from "react-router-dom";
import { toast } from "react-toastify";
import { useTranslation } from "react-i18next";

export default function SignInPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const { t } = useTranslation("auth");
    const toastShown = useRef(false);

    useEffect(() => {
        if (searchParams.get("verified") === "true" && !toastShown.current) {
            toastShown.current = true;
            toast.success(t("email_verified_toast"));
            void navigate("/sign-in", { replace: true });
        }
    }, [searchParams, t, navigate]);

    return (
        <Container maxWidth="sm">
            <SignInForm
                onSignUpRequest={() => {
                    void navigate("/sign-up", { replace: true });
                }}
                onSignInSuccess={() => {
                    void navigate("/", { replace: true });
                }}
                onForgotPassword={() => {
                    void navigate("/forgot-password");
                }}
            />
        </Container>
    );
}
