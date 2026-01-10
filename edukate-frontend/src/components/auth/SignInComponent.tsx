import { FormEvent, useEffect, useState } from 'react';
import { Box, TextField, Button, Typography, Link, FormControl, Divider } from '@mui/material';
import { useSignInMutation } from "../../http/requests/auth";
import { useNavigate } from "react-router-dom";
import { queryClient } from "../../http/queryClient";
import { SiteMark } from "../topbar/SiteMark";
import { SignCard, SignContainer } from "../Styled";

interface SignInComponentProps {
    onSignInSuccess?: () => void;
    onSignUpRequest?: () => void;
}

export const SignInComponent = ({onSignInSuccess, onSignUpRequest}: SignInComponentProps) => {
    const navigate = useNavigate();
    const signInMutation = useSignInMutation();
    useEffect(() => { if (signInMutation.isSuccess) {
        queryClient.refetchQueries({ queryKey: ["whoami"] }).finally(
            () => onSignInSuccess ? onSignInSuccess() : navigate("/")
        );
    } }, [signInMutation.isSuccess]);

    const [usernameErrorMessage, setUsernameErrorMessage] = useState("");
    const [passwordErrorMessage, setPasswordErrorMessage] = useState("");

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        if (!!usernameErrorMessage || !!passwordErrorMessage) {
            return;
        }
        const data = new FormData(event.currentTarget);
        signInMutation.mutate({ username: data.get("username") as string, password: data.get("password") as string});
    };

    const validateInputs = () => {
        const username = document.getElementById("username") as HTMLInputElement;
        const password = document.getElementById("password") as HTMLInputElement;

        let isValid = true;

        if (!username.value /* isValidUsername(username) */) {
            setUsernameErrorMessage("Please enter your username.");
            isValid = false;
        } else {
            setUsernameErrorMessage("");
        }

        if (!password.value || password.value.length < 3 /* isValidPassword(password) */) {
            setPasswordErrorMessage("Password must be at least 3 characters long.");
            isValid = false;
        } else {
            setPasswordErrorMessage("");
        }

        return isValid;
    };

    const signInTypoSx =  { width: "100%", fontSize: "clamp(2rem, 10vw, 2.15rem)", textAlign: "left" };
    const formSx = { display: "flex", flexDirection: "column", width: "100%", gap: 2 };
    return (
        <SignContainer direction="column" justifyContent="space-between">
            <SignCard variant="outlined">
                <SiteMark />
                <Typography component="h1" variant="h4" sx={signInTypoSx}>
                    Sign in
                </Typography>
                <Box component="form" onSubmit={handleSubmit} noValidate sx={formSx}>
                    <FormControl>
                        <TextField
                            error={!!usernameErrorMessage}
                            helperText={usernameErrorMessage}
                            id="username"
                            type="username"
                            name="username"
                            placeholder="username"
                            autoComplete="username"
                            label="Username"
                            autoFocus
                            required
                            fullWidth
                            variant="outlined"
                            color={usernameErrorMessage ? "error" : "primary"}
                        />
                    </FormControl>
                    <FormControl>
                        <TextField
                            error={!!passwordErrorMessage}
                            helperText={passwordErrorMessage}
                            name="password"
                            placeholder="••••••"
                            type="password"
                            id="password"
                            autoComplete="current-password"
                            label="Password"
                            required
                            fullWidth
                            variant="outlined"
                            color={passwordErrorMessage ? "error" : "primary"}
                        />
                    </FormControl>
                    <Button type="submit" fullWidth variant="contained" onClick={validateInputs}>
                        Sign in
                    </Button>
                </Box>
                <Divider>or</Divider>
                <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                    <Typography sx={{ textAlign: "center" }}>
                        Don't have an account?{" "}
                        <Link onClick={onSignUpRequest} variant="body2" sx={{ alignSelf: 'center', cursor: 'pointer' }}>
                            Sign up
                        </Link>
                    </Typography>
                </Box>
            </SignCard>
        </SignContainer>
    );
};
