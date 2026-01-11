import { SignUpComponent } from "../components/auth/SignUpComponent";
import { Container } from "@mui/material";
import { useNavigate } from "react-router-dom";

export default function SignUpView() {
    const navigate = useNavigate();
    return (
        <Container maxWidth="sm">
            <SignUpComponent
                onSignUpSuccess={() => navigate("/problems", {replace: true})}
                onSignInRequest={() => navigate("/sign-in", {replace: true})}
            />
        </Container>
    );
};
