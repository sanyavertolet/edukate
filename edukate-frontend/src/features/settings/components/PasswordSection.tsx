import { FC, ReactNode, useState } from "react";
import {
    Alert,
    Box,
    Button,
    IconButton,
    InputAdornment,
    LinearProgress,
    Link,
    Stack,
    TextField,
    Typography,
} from "@mui/material";
import VisibilityOutlinedIcon from "@mui/icons-material/VisibilityOutlined";
import VisibilityOffOutlinedIcon from "@mui/icons-material/VisibilityOffOutlined";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import RadioButtonUncheckedIcon from "@mui/icons-material/RadioButtonUnchecked";
import { isAxiosError } from "axios";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { validate } from "@/shared/utils/validation";
import { useChangePasswordMutation } from "../api";
import { passwordStrength, PasswordStrengthScore } from "../passwordStrength";

const MIN_PASSWORD = 6;
const MAX_PASSWORD = 128;

const STRENGTH_COLORS: Record<PasswordStrengthScore, "error" | "warning" | "info" | "success" | "inherit"> = {
    0: "inherit",
    1: "error",
    2: "warning",
    3: "info",
    4: "success",
};

/** A password TextField with a built-in reveal (show/hide) toggle. */
const PasswordField: FC<{
    label: string;
    value: string;
    autoComplete: string;
    error?: boolean;
    helperText?: ReactNode;
    autoFocus?: boolean;
    onChange: (value: string) => void;
    onBlur?: () => void;
}> = ({ label, value, autoComplete, error, helperText, autoFocus, onChange, onBlur }) => {
    const { t } = useTranslation("settings");
    const [reveal, setReveal] = useState(false);
    return (
        <TextField
            value={value}
            onChange={(e) => {
                onChange(e.target.value);
            }}
            onBlur={onBlur}
            error={error}
            helperText={helperText}
            label={label}
            type={reveal ? "text" : "password"}
            autoComplete={autoComplete}
            autoFocus={autoFocus}
            fullWidth
            slotProps={{
                input: {
                    endAdornment: (
                        <InputAdornment position="end">
                            <IconButton
                                size="small"
                                edge="end"
                                onClick={() => {
                                    setReveal((prev) => !prev);
                                }}
                                aria-label={t(reveal ? "hide_password" : "show_password")}
                            >
                                {reveal ? (
                                    <VisibilityOffOutlinedIcon fontSize="small" />
                                ) : (
                                    <VisibilityOutlinedIcon fontSize="small" />
                                )}
                            </IconButton>
                        </InputAdornment>
                    ),
                },
            }}
        />
    );
};

/** One live requirement row: a check/○ icon plus its label. */
const Requirement: FC<{ met: boolean; label: string }> = ({ met, label }) => (
    <Stack direction="row" spacing={1} alignItems="center">
        {met ? (
            <CheckCircleOutlineIcon color="success" sx={{ fontSize: 16 }} />
        ) : (
            <RadioButtonUncheckedIcon sx={{ fontSize: 16, color: "text.disabled" }} />
        )}
        <Typography variant="caption" color={met ? "text.secondary" : "text.disabled"}>
            {label}
        </Typography>
    </Stack>
);

export const PasswordSection: FC = () => {
    const { t } = useTranslation(["settings", "auth"]);
    const navigate = useNavigate();
    const mutation = useChangePasswordMutation();

    const [editing, setEditing] = useState(false);
    const [current, setCurrent] = useState("");
    const [next, setNext] = useState("");
    const [confirm, setConfirm] = useState("");
    const [currentError, setCurrentError] = useState<string | null>(null);
    const [nextError, setNextError] = useState<string | null>(null);
    const [confirmError, setConfirmError] = useState<string | null>(null);
    const [apiError, setApiError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    // Password error keys are split across namespaces (validation rules live in `auth`,
    // settings-specific ones like "wrong current password" in `settings`), so resolve each
    // against both, settings first.
    const errorText = (key: string | null): string | undefined => (key ? t([`settings:${key}`, `auth:${key}`]) : undefined);

    const lengthOk = next.length >= MIN_PASSWORD && next.length <= MAX_PASSWORD;
    const differsOk = next.length > 0 && next !== current;
    const matchOk = confirm.length > 0 && confirm === next;
    const strength = passwordStrength(next);
    const canSubmit = !!current && lengthOk && differsOk && matchOk;

    const reset = () => {
        setCurrent("");
        setNext("");
        setConfirm("");
        setCurrentError(null);
        setNextError(null);
        setConfirmError(null);
        setApiError(null);
    };

    const startEditing = () => {
        reset();
        setSuccess(false);
        setEditing(true);
    };

    const cancel = () => {
        reset();
        setEditing(false);
    };

    const handleSubmit = (e: React.SyntheticEvent) => {
        e.preventDefault();
        const cErr = current ? null : "password_required_error";
        const nErr = validate("password", next) ?? (next === current ? "password_same_as_current" : null);
        const matchErr = next === confirm ? null : "password_mismatch_error";
        setCurrentError(cErr);
        setNextError(nErr);
        setConfirmError(matchErr);
        if (cErr || nErr || matchErr) return;
        setApiError(null);
        mutation.mutate(
            { currentPassword: current, newPassword: next },
            {
                onSuccess: () => {
                    setSuccess(true);
                    setEditing(false);
                    reset();
                },
                onError: (error) => {
                    if (isAxiosError(error) && error.response?.status === 400) setCurrentError("password_wrong_current");
                    else setApiError("password_update_failed");
                },
            },
        );
    };

    return (
        <Box>
            {success && (
                <Alert
                    severity="success"
                    sx={{ mb: 2 }}
                    onClose={() => {
                        setSuccess(false);
                    }}
                >
                    {t("password_updated")}
                </Alert>
            )}
            {apiError && (
                <Alert
                    severity="error"
                    sx={{ mb: 2 }}
                    onClose={() => {
                        setApiError(null);
                    }}
                >
                    {t(apiError)}
                </Alert>
            )}

            {!editing ? (
                <Button variant="outlined" onClick={startEditing}>
                    {t("password_change_button")}
                </Button>
            ) : (
                <Stack component="form" onSubmit={handleSubmit} spacing={2}>
                    <PasswordField
                        label={t("current_password_label")}
                        value={current}
                        autoComplete="current-password"
                        autoFocus
                        error={!!currentError}
                        helperText={errorText(currentError)}
                        onChange={(v) => {
                            setCurrent(v);
                            if (currentError) setCurrentError(null);
                        }}
                    />

                    <Box>
                        <PasswordField
                            label={t("new_password_label")}
                            value={next}
                            autoComplete="new-password"
                            error={!!nextError}
                            helperText={errorText(nextError)}
                            onChange={(v) => {
                                setNext(v);
                                if (nextError) setNextError(null);
                            }}
                        />
                        {next.length > 0 && (
                            <Box sx={{ mt: 1 }}>
                                <Stack direction="row" alignItems="center" spacing={1}>
                                    <LinearProgress
                                        variant="determinate"
                                        value={strength.score * 25}
                                        color={STRENGTH_COLORS[strength.score]}
                                        sx={{ flexGrow: 1, height: 6, borderRadius: 3 }}
                                    />
                                    <Typography variant="caption" color="text.secondary" sx={{ minWidth: 48 }}>
                                        {strength.labelKey ? t(strength.labelKey) : ""}
                                    </Typography>
                                </Stack>
                                <Stack spacing={0.5} sx={{ mt: 1 }}>
                                    <Requirement met={lengthOk} label={t("pw_req_length")} />
                                    <Requirement met={differsOk} label={t("pw_req_differs")} />
                                </Stack>
                            </Box>
                        )}
                    </Box>

                    <PasswordField
                        label={t("confirm_password_label")}
                        value={confirm}
                        autoComplete="new-password"
                        error={!!confirmError}
                        helperText={errorText(confirmError)}
                        onChange={(v) => {
                            setConfirm(v);
                            if (confirmError) setConfirmError(null);
                        }}
                        onBlur={() => {
                            setConfirmError(confirm.length === 0 || confirm === next ? null : "password_mismatch_error");
                        }}
                    />

                    <Stack direction="row" alignItems="center" justifyContent="space-between">
                        <Link
                            component="button"
                            type="button"
                            variant="body2"
                            onClick={() => {
                                void navigate("/forgot-password");
                            }}
                            sx={{ cursor: "pointer" }}
                        >
                            {t("forgot_password", { ns: "auth" })}
                        </Link>
                        <Stack direction="row" spacing={1}>
                            <Button onClick={cancel} disabled={mutation.isPending}>
                                {t("cancel")}
                            </Button>
                            <Button type="submit" variant="contained" disabled={!canSubmit || mutation.isPending}>
                                {t("password_update_button")}
                            </Button>
                        </Stack>
                    </Stack>
                </Stack>
            )}
        </Box>
    );
};
