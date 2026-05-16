import { FocusEvent, SyntheticEvent, useState } from "react";
import { Box, Button, CircularProgress, TextField, Typography } from "@mui/material";
import { useResetPasswordMutation } from "@/features/auth/api";
import { SignCard, SignContainer } from "@/shared/components/Styled";
import { SiteMark } from "@/shared/components/layout/topbar/SiteMark";
import { useTranslation } from "react-i18next";
import { validate } from "@/shared/utils/validation";

const titleSx = { width: "100%", fontSize: "clamp(2rem, 10vw, 2.15rem)", textAlign: "left" } as const;
const formSx = { display: "flex", flexDirection: "column", gap: 2 } as const;

type ResetPasswordFormProps = {
    token: string;
    onSuccess?: () => void;
};

export const ResetPasswordForm = ({ token, onSuccess }: ResetPasswordFormProps) => {
    const { t } = useTranslation("auth");
    const resetPasswordMutation = useResetPasswordMutation();

    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [passwordError, setPasswordError] = useState<string | null>(null);
    const [confirmPasswordError, setConfirmPasswordError] = useState<string | null>(null);

    const validatePassword = (value: string) => validate("password", value);
    const validateConfirmPassword = (confirm: string, against: string) =>
        confirm === against ? null : "password_mismatch_error";

    const handleBlurPassword = (e: FocusEvent<HTMLInputElement>) => {
        setPasswordError(validatePassword(e.target.value));
        if (confirmPassword) setConfirmPasswordError(validateConfirmPassword(confirmPassword, e.target.value));
    };
    const handleBlurConfirmPassword = (e: FocusEvent<HTMLInputElement>) => {
        setConfirmPasswordError(validateConfirmPassword(e.target.value, newPassword));
    };

    const handleSubmit = (event: SyntheticEvent) => {
        event.preventDefault();
        const pErr = validatePassword(newPassword);
        const cErr = validateConfirmPassword(confirmPassword, newPassword);
        setPasswordError(pErr);
        setConfirmPasswordError(cErr);
        if (pErr || cErr) return;

        resetPasswordMutation.mutate({ token, newPassword }, { onSuccess });
    };

    return (
        <SignContainer direction="column" justifyContent="space-between">
            <SignCard variant="outlined">
                <SiteMark />
                <Typography component="h1" variant="h4" sx={titleSx}>
                    {t("reset_password_title")}
                </Typography>
                <Box component="form" onSubmit={handleSubmit} noValidate sx={formSx}>
                    <TextField
                        value={newPassword}
                        onChange={(e) => {
                            setNewPassword(e.target.value);
                        }}
                        onBlur={handleBlurPassword}
                        error={!!passwordError}
                        helperText={passwordError ? t(passwordError) : " "}
                        name="new-password"
                        type="password"
                        placeholder={t("password_placeholder")}
                        autoComplete="new-password"
                        label={t("new_password_label")}
                        autoFocus
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
                        loading={resetPasswordMutation.isPending}
                        loadingPosition="center"
                        loadingIndicator={<CircularProgress size={20} color="inherit" />}
                    >
                        {t("reset_password_submit")}
                    </Button>
                </Box>
            </SignCard>
        </SignContainer>
    );
};
