import { FocusEvent, FormEvent, useState } from "react";
import { Box, Button, Link, TextField, Typography } from "@mui/material";
import { useSignInMutation } from "@/features/auth/api";
import { useNavigate } from "react-router-dom";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import { SiteMark } from "@/shared/components/layout/topbar/SiteMark";
import { SignCard, SignContainer } from "@/shared/components/Styled";

const titleSx = { width: "100%", fontSize: "clamp(2rem, 10vw, 2.15rem)", textAlign: "left" } as const;
const formSx = { display: "flex", flexDirection: "column", width: "100%", gap: 2 } as const;
const footerSx = { display: "flex", flexDirection: "column", gap: 2 } as const;

interface SignInFormProps {
    onSignInSuccess?: () => void;
    onSignUpRequest?: () => void;
}

export const SignInForm = ({ onSignInSuccess, onSignUpRequest }: SignInFormProps) => {
    const navigate = useNavigate();
    const signInMutation = useSignInMutation();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [usernameError, setUsernameError] = useState<string | null>(null);
    const [passwordError, setPasswordError] = useState<string | null>(null);

    const validateUsername = (value: string) => (value.trim() ? null : "Please enter your username.");
    const validatePassword = (value: string) => (value.trim() ? null : "Please enter your password.");

    const handleBlurUsername = (e: FocusEvent<HTMLInputElement>) => setUsernameError(validateUsername(e.target.value));
    const handleBlurPassword = (e: FocusEvent<HTMLInputElement>) => setPasswordError(validatePassword(e.target.value));

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const uErr = validateUsername(username);
        const pErr = validatePassword(password);
        setUsernameError(uErr);
        setPasswordError(pErr);
        if (uErr || pErr) return;

        signInMutation.mutate(
            { username, password },
            {
                onSuccess: () =>
                    queryClient
                        .refetchQueries({ queryKey: queryKeys.auth.whoami })
                        .finally(() => (onSignInSuccess ? onSignInSuccess() : navigate("/"))),
            },
        );
    };

    return (
        <SignContainer direction="column" justifyContent="space-between">
            <SignCard variant="outlined">
                <SiteMark />
                <Typography component="h1" variant="h4" sx={titleSx}>
                    Sign in
                </Typography>
                <Box component="form" onSubmit={handleSubmit} noValidate sx={formSx}>
                    <TextField
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
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
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        onBlur={handleBlurPassword}
                        error={!!passwordError}
                        helperText={passwordError ?? " "}
                        name="password"
                        type="password"
                        placeholder="••••••"
                        autoComplete="current-password"
                        label="Password"
                        required
                        fullWidth
                        variant="outlined"
                    />
                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        disabled={signInMutation.isPending}
                    >
                        Sign in
                    </Button>
                </Box>
                <Box sx={footerSx}>
                    <Typography sx={{ textAlign: "center" }}>
                        Don&apos;t have an account?{" "}
                        <Link onClick={onSignUpRequest} variant="body2" sx={{ alignSelf: "center", cursor: "pointer" }}>
                            Sign up
                        </Link>
                    </Typography>
                </Box>
            </SignCard>
        </SignContainer>
    );
};
