import { SignUpForm } from "@/features/auth/components/SignUpForm";
import { Container } from "@mui/material";
import { useNavigate } from "react-router-dom";

export default function SignUpPage() {
    const navigate = useNavigate();
    return (
        <Container maxWidth="sm">
            <SignUpForm
                onSignUpSuccess={() => { void navigate("/problems", { replace: true }); }}
                onSignInRequest={() => { void navigate("/sign-in", { replace: true }); }}
            />
        </Container>
    );
}
