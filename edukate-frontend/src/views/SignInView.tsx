import { SignInComponent } from "../components/auth/SignInComponent";
import { Container, Paper, Typography } from "@mui/material";

export default function SignInView() {
    return (
        <Container maxWidth="sm">
            <Paper sx={{ padding: 4 }}>
                <Typography color="secondary" variant="h5">
                    Sign In
                </Typography>
                <SignInComponent/>
            </Paper>
        </Container>
    );
};
