import { Container } from "@mui/material";
import { SignInForm } from "@/features/auth/components/SignInForm";
import { useNavigate } from "react-router-dom";

export default function SignInPage() {
    const navigate = useNavigate();
    return (
        <Container maxWidth="sm">
            <SignInForm
                onSignUpRequest={() => navigate("/sign-up", { replace: true })}
                onSignInSuccess={() => navigate(-1)}
            />
        </Container>
    );
}
