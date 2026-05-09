import { FocusEvent, SyntheticEvent, useState } from "react";
import { Box, Button, Link, TextField, Typography } from "@mui/material";
import { useSignInMutation } from "@/features/auth/api";
import { useNavigate } from "react-router-dom";
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
}

export const SignInForm = ({ onSignInSuccess, onSignUpRequest }: SignInFormProps) => {
    const { t } = useTranslation("auth");
    const navigate = useNavigate();
    const signInMutation = useSignInMutation();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [usernameError, setUsernameError] = useState<string | null>(null);
    const [passwordError, setPasswordError] = useState<string | null>(null);

    const validateUsername = (value: string) => (value.trim() ? null : t("username_required_error"));
    const validatePassword = (value: string) => (value.trim() ? null : t("password_required_error"));

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

        signInMutation.mutate(
            { username, password },
            {
                onSuccess: () => {
                    void queryClient.refetchQueries({ queryKey: queryKeys.auth.whoami }).finally(() => {
                        if (onSignInSuccess) onSignInSuccess();
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
                    {t("sign_in_title")}
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
                        placeholder={t("username_placeholder")}
                        autoComplete="username"
                        label={t("username_label")}
                        autoFocus
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
                        placeholder={t("password_placeholder")}
                        autoComplete="current-password"
                        label={t("password_label")}
                        required
                        fullWidth
                        variant="outlined"
                    />
                    <Button type="submit" fullWidth variant="contained" disabled={signInMutation.isPending}>
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
