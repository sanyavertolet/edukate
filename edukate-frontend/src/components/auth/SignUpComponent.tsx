import { FormEvent, useEffect, useState } from "react";
import { Box, TextField, Button, Typography, Link, FormControl } from '@mui/material';
import { useSignUpMutation } from "../../http/requests/auth";
import { useNavigate } from "react-router-dom";
import { queryClient } from "../../http/queryClient";
import { SignCard, SignContainer } from "../Styled";
import { SiteMark } from "../topbar/SiteMark";

type SignUpComponentProps = {
    onSignInRequest?: () => void,
    onSignUpSuccess?: () => void,
};

export const SignUpComponent = ({onSignInRequest, onSignUpSuccess}: SignUpComponentProps) => {
    const navigate = useNavigate();
    const signUpMutation = useSignUpMutation();
    useEffect(() => { if (signUpMutation.isSuccess) {
        queryClient.refetchQueries({ queryKey: ["whoami"] }).finally(
            () => onSignUpSuccess ? onSignUpSuccess() : navigate("/")
        )
    }}, [signUpMutation.isSuccess]);

    const [emailErrorMessage, setEmailErrorMessage] = useState("");
    const [passwordErrorMessage, setPasswordErrorMessage] = useState("");
    const [usernameErrorMessage, setUsernameErrorMessage] = useState("");

    const validateInputs = () => {
        const email = document.getElementById("email") as HTMLInputElement;
        const password = document.getElementById("password") as HTMLInputElement;
        const username = document.getElementById("username") as HTMLInputElement;

        let isValid = true;

        if (!email.value || !/\S+@\S+\.\S+/.test(email.value) /* isValidEmail(email) */) {
            setEmailErrorMessage("Please enter a valid email address.");
            isValid = false;
        } else {
            setEmailErrorMessage("");
        }

        if (!password.value || password.value.length < 3 /* isValidPassword(password) */) {
            setPasswordErrorMessage("Password must be at least 3 characters long.");
            isValid = false;
        } else {
            setPasswordErrorMessage("");
        }

        if (!username.value || username.value.length < 1 /* isValidUsername(username) */) {
            setUsernameErrorMessage("Username is required.");
            isValid = false;
        } else {
            setUsernameErrorMessage("");
        }

        return isValid;
    };

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        if (!!usernameErrorMessage || !!emailErrorMessage || !!passwordErrorMessage) {
            return;
        }
        const data = new FormData(event.currentTarget);
        signUpMutation.mutate({
            username: data.get("username") as string,
            email: data.get("email") as string,
            password: data.get("password") as string,
        });
    };

    return (
        <SignContainer direction="column" justifyContent="space-between">
            <SignCard variant="outlined">
                <SiteMark />
                <Typography
                    component="h1"
                    variant="h4"
                    sx={{ width: "100%", fontSize: "clamp(2rem, 10vw, 2.15rem)", textAlign: "left" }}
                >
                    Sign up
                </Typography>
                <Box
                    component="form"
                    onSubmit={handleSubmit}
                    sx={{ display: "flex", flexDirection: "column", gap: 2 }}
                >
                    <FormControl>
                        <TextField
                            id="username"
                            name="username"
                            type="username"
                            placeholder="username"
                            autoComplete="username"
                            label="Username"
                            required
                            fullWidth
                            autoFocus
                            error={!!usernameErrorMessage}
                            helperText={usernameErrorMessage}
                            color={usernameErrorMessage ? "error" : "primary"}
                        />
                    </FormControl>
                    <FormControl>
                        <TextField
                            id="email"
                            name="email"
                            type="email"
                            placeholder="your@email.com"
                            autoComplete="email"
                            label={"Email"}
                            variant="outlined"
                            required
                            fullWidth
                            error={!!emailErrorMessage}
                            helperText={emailErrorMessage}
                            color={emailErrorMessage ? "error" : "primary"}
                        />
                    </FormControl>
                    <FormControl>
                        <TextField
                            id="password"
                            name="password"
                            type="password"
                            placeholder="••••••"
                            autoComplete="new-password"
                            label={"Password"}
                            variant="outlined"
                            required
                            fullWidth
                            error={!!passwordErrorMessage}
                            helperText={passwordErrorMessage}
                            color={passwordErrorMessage ? "error" : "primary"}
                        />
                    </FormControl>
                    <Button type="submit" fullWidth variant="contained" onClick={validateInputs}>
                        Sign up
                    </Button>
                </Box>
                <Box sx={{ display: 'flex', flexDirection: "column", gap: 2 }}>
                    <Typography sx={{ textAlign: "center" }}>
                        Already have an account?{" "}
                        <Link onClick={onSignInRequest} variant="body2" sx={{ alignSelf: "center", cursor: "pointer" }}>
                            Sign in
                        </Link>
                    </Typography>
                </Box>
            </SignCard>
        </SignContainer>
    );
};
