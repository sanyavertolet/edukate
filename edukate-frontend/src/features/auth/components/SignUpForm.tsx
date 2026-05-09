import { FocusEvent, SyntheticEvent, useState } from "react";
import { Box, Button, Link, TextField, Typography } from "@mui/material";
import { useSignUpMutation } from "@/features/auth/api";
import { useNavigate } from "react-router-dom";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import { SignCard, SignContainer } from "@/shared/components/Styled";
import { SiteMark } from "@/shared/components/layout/topbar/SiteMark";
import { validate } from "@/shared/utils/validation";
import { useTranslation } from "react-i18next";

const titleSx = { width: "100%", fontSize: "clamp(2rem, 10vw, 2.15rem)", textAlign: "left" } as const;
const formSx = { display: "flex", flexDirection: "column", gap: 2 } as const;
const footerSx = { display: "flex", flexDirection: "column", gap: 2 } as const;

type SignUpFormProps = {
    onSignInRequest?: () => void;
    onSignUpSuccess?: () => void;
};

export const SignUpForm = ({ onSignInRequest, onSignUpSuccess }: SignUpFormProps) => {
    const { t } = useTranslation("auth");
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

    const handleSubmit = (event: SyntheticEvent) => {
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
                    {t("sign_up_title")}
                </Typography>
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
                        placeholder={t("username_placeholder")}
                        autoComplete="username"
                        label={t("username_label")}
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
                        helperText={emailError ? t(emailError) : " "}
                        name="email"
                        type="email"
                        placeholder={t("email_placeholder")}
                        autoComplete="email"
                        label={t("email_label")}
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
                        helperText={passwordError ? t(passwordError) : " "}
                        name="password"
                        type="password"
                        placeholder={t("password_placeholder")}
                        autoComplete="new-password"
                        label={t("password_label")}
                        required
                        fullWidth
                        variant="outlined"
                    />
                    <Button type="submit" fullWidth variant="contained" disabled={signUpMutation.isPending}>
                        {t("sign_up_button")}
                    </Button>
                </Box>
                <Box sx={footerSx}>
                    <Typography sx={{ textAlign: "center" }}>
                        {t("already_have_account")}{" "}
                        <Link onClick={onSignInRequest} variant="body2" sx={{ alignSelf: "center", cursor: "pointer" }}>
                            {t("sign_in_link")}
                        </Link>
                    </Typography>
                </Box>
            </SignCard>
        </SignContainer>
    );
};
