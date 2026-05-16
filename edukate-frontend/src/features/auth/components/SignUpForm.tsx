import { FocusEvent, SyntheticEvent, useState } from "react";
import { Alert, Box, Button, CircularProgress, Link, TextField, Typography } from "@mui/material";
import { isAxiosError } from "axios";
import { useSignUpMutation } from "@/features/auth/api";
import { SignCard, SignContainer } from "@/shared/components/Styled";
import { SiteMark } from "@/shared/components/layout/topbar/SiteMark";
import { validate } from "@/shared/utils/validation";
import { useTranslation } from "react-i18next";

const titleSx = { width: "100%", fontSize: "clamp(2rem, 10vw, 2.15rem)", textAlign: "left" } as const;
const formSx = { display: "flex", flexDirection: "column", gap: 2 } as const;
const footerSx = { display: "flex", flexDirection: "column", gap: 2 } as const;

type SignUpFormProps = {
    onSignInRequest?: () => void;
};

export const SignUpForm = ({ onSignInRequest }: SignUpFormProps) => {
    const { t } = useTranslation("auth");
    const signUpMutation = useSignUpMutation();

    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [usernameError, setUsernameError] = useState<string | null>(null);
    const [emailError, setEmailError] = useState<string | null>(null);
    const [passwordError, setPasswordError] = useState<string | null>(null);
    const [confirmPasswordError, setConfirmPasswordError] = useState<string | null>(null);
    const [apiError, setApiError] = useState<string | null>(null);
    const [submitted, setSubmitted] = useState(false);

    const validateConfirmPassword = (confirm: string, against: string) =>
        confirm === against ? null : "password_mismatch_error";

    const handleBlurUsername = (e: FocusEvent<HTMLInputElement>) => {
        setUsernameError(validate("username", e.target.value));
    };
    const handleBlurEmail = (e: FocusEvent<HTMLInputElement>) => {
        setEmailError(validate("email", e.target.value));
    };
    const handleBlurPassword = (e: FocusEvent<HTMLInputElement>) => {
        setPasswordError(validate("password", e.target.value));
        if (confirmPassword) setConfirmPasswordError(validateConfirmPassword(confirmPassword, e.target.value));
    };
    const handleBlurConfirmPassword = (e: FocusEvent<HTMLInputElement>) => {
        setConfirmPasswordError(validateConfirmPassword(e.target.value, password));
    };

    const handleSubmit = (event: SyntheticEvent) => {
        event.preventDefault();
        const uErr = validate("username", username);
        const eErr = validate("email", email);
        const pErr = validate("password", password);
        const cErr = validateConfirmPassword(confirmPassword, password);
        setUsernameError(uErr);
        setEmailError(eErr);
        setPasswordError(pErr);
        setConfirmPasswordError(cErr);
        if (uErr || eErr || pErr || cErr) return;

        setApiError(null);
        signUpMutation.mutate(
            { username, email, password },
            {
                onSuccess: () => {
                    setSubmitted(true);
                },
                onError: (error) => {
                    if (isAxiosError(error) && error.response?.status === 409) {
                        setUsernameError("sign_up_conflict_error");
                    } else {
                        setApiError("sign_up_error");
                    }
                },
            },
        );
    };

    if (submitted) {
        return (
            <SignContainer direction="column" justifyContent="space-between">
                <SignCard variant="outlined">
                    <SiteMark />
                    <Typography component="h1" variant="h4" sx={titleSx}>
                        {t("verify_email_prompt")}
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                        {t("verify_email_subtitle")}
                    </Typography>
                    <Button variant="text" onClick={onSignInRequest}>
                        {t("sign_in_link")}
                    </Button>
                </SignCard>
            </SignContainer>
        );
    }

    return (
        <SignContainer direction="column" justifyContent="space-between">
            <SignCard variant="outlined">
                <SiteMark />
                <Typography component="h1" variant="h4" sx={titleSx}>
                    {t("sign_up_title")}
                </Typography>
                {apiError && <Alert severity="error">{t(apiError)}</Alert>}
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
                    <TextField
                        value={confirmPassword}
                        onChange={(e) => {
                            setConfirmPassword(e.target.value);
                        }}
                        onBlur={handleBlurConfirmPassword}
                        error={!!confirmPasswordError}
                        helperText={confirmPasswordError ? t(confirmPasswordError) : " "}
                        name="confirm-password"
                        type="password"
                        placeholder={t("password_placeholder")}
                        autoComplete="new-password"
                        label={t("confirm_password_label")}
                        required
                        fullWidth
                        variant="outlined"
                    />
                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        loading={signUpMutation.isPending}
                        loadingPosition="center"
                        loadingIndicator={<CircularProgress size={20} color="inherit" />}
                    >
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
