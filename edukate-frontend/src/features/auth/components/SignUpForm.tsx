import { FocusEvent, FormEvent, useState } from "react";
import { Box, Button, Link, TextField, Typography } from "@mui/material";
import { useSignUpMutation } from "@/features/auth/api";
import { useNavigate } from "react-router-dom";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import { SignCard, SignContainer } from "@/shared/components/Styled";
import { SiteMark } from "@/shared/components/layout/topbar/SiteMark";
import { validate } from "@/shared/utils/validation";

const titleSx = { width: "100%", fontSize: "clamp(2rem, 10vw, 2.15rem)", textAlign: "left" } as const;
const formSx = { display: "flex", flexDirection: "column", gap: 2 } as const;
const footerSx = { display: "flex", flexDirection: "column", gap: 2 } as const;

type SignUpFormProps = {
    onSignInRequest?: () => void;
    onSignUpSuccess?: () => void;
};

export const SignUpForm = ({ onSignInRequest, onSignUpSuccess }: SignUpFormProps) => {
    const navigate = useNavigate();
    const signUpMutation = useSignUpMutation();

    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [usernameError, setUsernameError] = useState<string | null>(null);
    const [emailError, setEmailError] = useState<string | null>(null);
    const [passwordError, setPasswordError] = useState<string | null>(null);

    const handleBlurUsername = (e: FocusEvent<HTMLInputElement>) => {
        setUsernameError(validate("username", e.target.value));
    };
    const handleBlurEmail = (e: FocusEvent<HTMLInputElement>) => {
        setEmailError(validate("email", e.target.value));
    };
    const handleBlurPassword = (e: FocusEvent<HTMLInputElement>) => {
        setPasswordError(validate("password", e.target.value));
    };

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const uErr = validate("username", username);
        const eErr = validate("email", email);
        const pErr = validate("password", password);
        setUsernameError(uErr);
        setEmailError(eErr);
        setPasswordError(pErr);
        if (uErr || eErr || pErr) return;

        signUpMutation.mutate(
            { username, email, password },
            {
                onSuccess: () => {
                    void queryClient.refetchQueries({ queryKey: queryKeys.auth.whoami }).finally(() => {
                        if (onSignUpSuccess) onSignUpSuccess();
                        else void navigate("/");
                    });
                },
            },
        );
    };

    return (
        <SignContainer direction="column" justifyContent="space-between">
            <SignCard variant="outlined">
                <SiteMark />
                <Typography component="h1" variant="h4" sx={titleSx}>
                    Sign up
                </Typography>
                <Box component="form" onSubmit={handleSubmit} noValidate sx={formSx}>
                    <TextField
                        value={username}
                        onChange={(e) => {
                            setUsername(e.target.value);
                        }}
                        onBlur={handleBlurUsername}
                        error={!!usernameError}
                        helperText={usernameError ?? " "}
                        name="username"
                        type="text"
                        placeholder="username"
                        autoComplete="username"
                        label="Username"
                        autoFocus
                        required
                        fullWidth
                        variant="outlined"
                    />
                    <TextField
                        value={email}
                        onChange={(e) => {
                            setEmail(e.target.value);
                        }}
                        onBlur={handleBlurEmail}
                        error={!!emailError}
                        helperText={emailError ?? " "}
                        name="email"
                        type="email"
                        placeholder="your@email.com"
                        autoComplete="email"
                        label="Email"
                        required
                        fullWidth
                        variant="outlined"
                    />
                    <TextField
                        value={password}
                        onChange={(e) => {
                            setPassword(e.target.value);
                        }}
                        onBlur={handleBlurPassword}
                        error={!!passwordError}
                        helperText={passwordError ?? " "}
                        name="password"
                        type="password"
                        placeholder="••••••"
                        autoComplete="new-password"
                        label="Password"
                        required
                        fullWidth
                        variant="outlined"
                    />
                    <Button type="submit" fullWidth variant="contained" disabled={signUpMutation.isPending}>
                        Sign up
                    </Button>
                </Box>
                <Box sx={footerSx}>
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
