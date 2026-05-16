import { FocusEvent, SyntheticEvent, useState } from "react";
import { Box, Button, CircularProgress, TextField, Typography } from "@mui/material";
import { useForgotPasswordMutation } from "@/features/auth/api";
import { SignCard, SignContainer } from "@/shared/components/Styled";
import { SiteMark } from "@/shared/components/layout/topbar/SiteMark";
import { validate } from "@/shared/utils/validation";
import { useTranslation } from "react-i18next";

const titleSx = { width: "100%", fontSize: "clamp(2rem, 10vw, 2.15rem)", textAlign: "left" } as const;
const formSx = { display: "flex", flexDirection: "column", gap: 2 } as const;

type ForgotPasswordFormProps = {
    onBack?: () => void;
};

export const ForgotPasswordForm = ({ onBack }: ForgotPasswordFormProps) => {
    const { t } = useTranslation("auth");
    const forgotPasswordMutation = useForgotPasswordMutation();

    const [email, setEmail] = useState("");
    const [emailError, setEmailError] = useState<string | null>(null);
    const [submitted, setSubmitted] = useState(false);

    const handleBlurEmail = (e: FocusEvent<HTMLInputElement>) => {
        setEmailError(validate("email", e.target.value));
    };

    const handleSubmit = (event: SyntheticEvent) => {
        event.preventDefault();
        const eErr = validate("email", email);
        setEmailError(eErr);
        if (eErr) return;

        forgotPasswordMutation.mutate(
            { email },
            {
                onSuccess: () => {
                    setSubmitted(true);
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
                        {t("forgot_password_success")}
                    </Typography>
                    <Button variant="text" onClick={onBack}>
                        {t("forgot_password_back")}
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
                    {t("forgot_password_title")}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    {t("forgot_password_description")}
                </Typography>
                <Box component="form" onSubmit={handleSubmit} noValidate sx={formSx}>
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
                        autoFocus
                        required
                        fullWidth
                        variant="outlined"
                    />
                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        loading={forgotPasswordMutation.isPending}
                        loadingPosition="center"
                        loadingIndicator={<CircularProgress size={20} color="inherit" />}
                    >
                        {t("forgot_password_submit")}
                    </Button>
                    <Button variant="text" onClick={onBack}>
                        {t("forgot_password_back")}
                    </Button>
                </Box>
            </SignCard>
        </SignContainer>
    );
};
