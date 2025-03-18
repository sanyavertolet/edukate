import { SignUpComponent } from "../components/auth/SignUpComponent";
import { Container, Paper, Typography } from "@mui/material";

export default function SignUpView() {
    return (
        <Container maxWidth="sm">
            <Paper sx={{ padding: 4 }}>
                <Typography color="secondary" variant="h5">
                    Sign Up
                </Typography>
                <SignUpComponent/>
            </Paper>
        </Container>
    );
};
