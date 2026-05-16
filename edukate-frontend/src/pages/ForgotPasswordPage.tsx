import { Container } from "@mui/material";
import { ForgotPasswordForm } from "@/features/auth/components/ForgotPasswordForm";
import { useNavigate } from "react-router-dom";

export default function ForgotPasswordPage() {
    const navigate = useNavigate();
    return (
        <Container maxWidth="sm">
            <ForgotPasswordForm
                onBack={() => {
                    void navigate("/sign-in", { replace: true });
                }}
            />
        </Container>
    );
}
