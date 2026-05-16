import { FocusEvent, SyntheticEvent, useState } from "react";
import { Alert, Box, Button, CircularProgress, Link, TextField, Typography } from "@mui/material";
import { isAxiosError } from "axios";
import { useSignInMutation } from "@/features/auth/api";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import { SiteMark } from "@/shared/components/layout/topbar/SiteMark";
import { SignCard, SignContainer } from "@/shared/components/Styled";
import { useTranslation } from "react-i18next";

const titleSx = { width: "100%", fontSize: "clamp(2rem, 10vw, 2.15rem)", textAlign: "left" } as const;
const formSx = { display: "flex", flexDirection: "column", width: "100%", gap: 2 } as const;
const footerSx = { display: "flex", flexDirection: "column", gap: 2 } as const;

interface SignInFormProps {
    onSignInSuccess?: () => void;
    onSignUpRequest?: () => void;
    onForgotPassword?: () => void;
}

export const SignInForm = ({ onSignInSuccess = () => {}, onSignUpRequest, onForgotPassword }: SignInFormProps) => {
    const { t } = useTranslation("auth");
    const signInMutation = useSignInMutation();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [usernameError, setUsernameError] = useState<string | null>(null);
    const [passwordError, setPasswordError] = useState<string | null>(null);
    const [signInError, setSignInError] = useState<string | null>(null);

    // Store translation keys (not translated strings) so errors re-translate on language switch
    const validateUsername = (value: string) => (value.trim() ? null : "login_required_error");
    const validatePassword = (value: string) => (value.trim() ? null : "password_required_error");

    const handleBlurUsername = (e: FocusEvent<HTMLInputElement>) => {
        setUsernameError(validateUsername(e.target.value));
    };
    const handleBlurPassword = (e: FocusEvent<HTMLInputElement>) => {
        setPasswordError(validatePassword(e.target.value));
    };

    const handleSubmit = (event: SyntheticEvent) => {
        event.preventDefault();
        const uErr = validateUsername(username);
        const pErr = validatePassword(password);
        setUsernameError(uErr);
        setPasswordError(pErr);
        if (uErr || pErr) return;

        setSignInError(null);
        signInMutation.mutate(
            { username, password },
            {
                onSuccess: () => {
                    void queryClient.refetchQueries({ queryKey: queryKeys.auth.whoami }).finally(onSignInSuccess);
                },
                onError: (error) => {
                    if (isAxiosError(error) && error.response?.status === 423) {
                        setSignInError("sign_in_unverified");
                    } else {
                        setSignInError("sign_in_invalid_credentials");
                    }
                },
            },
        );
    };

    return (
        <SignContainer direction="column" justifyContent="space-between">
            <SignCard variant="outlined">
                <SiteMark />
                <Typography component="h1" variant="h4" sx={titleSx}>
                    {t("sign_in_title")}
                </Typography>
                {signInError && <Alert severity="error">{t(signInError)}</Alert>}
                <Box component="form" onSubmit={handleSubmit} noValidate sx={formSx}>
                    <TextField
                        value={username}
                        onChange={(e) => {
                            setUsername(e.target.value);
                        }}
                        onBlur={handleBlurUsername}
                        error={!!usernameError}
                        helperText={usernameError ? t(usernameError) : " "}
                        name="username"
                        type="text"
                        placeholder={t("login_placeholder")}
                        autoComplete="username"
                        label={t("login_label")}
                        autoFocus
                        required
                        fullWidth
                        variant="outlined"
                    />
                    <Box>
                        <TextField
                            value={password}
                            onChange={(e) => {
                                setPassword(e.target.value);
                            }}
                            onBlur={handleBlurPassword}
                            error={!!passwordError}
                            helperText={passwordError ? t(passwordError) : " "}
                            name="password"
                            type="password"
                            placeholder={t("password_placeholder")}
                            autoComplete="current-password"
                            label={t("password_label")}
                            required
                            fullWidth
                            variant="outlined"
                        />
                        <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                            <Link
                                component="button"
                                type="button"
                                variant="body2"
                                onClick={onForgotPassword}
                                sx={{ cursor: "pointer" }}
                            >
                                {t("forgot_password")}
                            </Link>
                        </Box>
                    </Box>
                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        loading={signInMutation.isPending}
                        loadingPosition="center"
                        loadingIndicator={<CircularProgress size={20} color="inherit" />}
                    >
                        {t("sign_in_button")}
                    </Button>
                </Box>
                <Box sx={footerSx}>
                    <Typography sx={{ textAlign: "center" }}>
                        {t("dont_have_account")}{" "}
                        <Link onClick={onSignUpRequest} variant="body2" sx={{ alignSelf: "center", cursor: "pointer" }}>
                            {t("sign_up_link")}
                        </Link>
                    </Typography>
                </Box>
            </SignCard>
        </SignContainer>
    );
};
