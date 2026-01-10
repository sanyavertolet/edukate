import { Container } from "@mui/material";
import { SignInComponent } from "../components/auth/SignInComponent";
import { useNavigate } from "react-router-dom";

export default function SignInView() {
    const navigate = useNavigate();
    return (
        <Container maxWidth="sm">
            <SignInComponent
                onSignUpRequest={() => navigate("/sign-up", {replace: true})}
                onSignInSuccess={() => navigate(-1)}
            />
        </Container>
    );
};
