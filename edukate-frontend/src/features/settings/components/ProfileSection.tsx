import { FC, KeyboardEvent, useLayoutEffect, useRef, useState } from "react";
import {
    Alert,
    Avatar,
    Box,
    Divider,
    IconButton,
    InputAdornment,
    Stack,
    TextField,
    Tooltip,
    Typography,
} from "@mui/material";
import PhotoCameraOutlinedIcon from "@mui/icons-material/PhotoCameraOutlined";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import CheckIcon from "@mui/icons-material/Check";
import CloseIcon from "@mui/icons-material/Close";
import { isAxiosError } from "axios";
import { useTranslation } from "react-i18next";
import { useAuthContext } from "@/features/auth/context";
import { useDeviceContext } from "@/shared/context/DeviceContext";
import { validate } from "@/shared/utils/validation";
import { DEFAULT_MAX_MEGAPIXELS, FileValidationRules, validateFile } from "@/shared/utils/fileValidation";
import {
    useChangeUsernameMutation,
    useDeleteAvatarMutation,
    useRequestEmailChangeMutation,
    useUploadAvatarMutation,
} from "../api";
import { AvatarCropDialog } from "./AvatarCropDialog";
import { PasswordSection } from "./PasswordSection";

// Fixed avatar size on mobile (stacked layout) and the fallback before the desktop
// field-column height has been measured.
const AVATAR_PIXELS = 96;

// Per-file guards for the avatar pick, enforced by the shared validateFile pipeline.
// 10 MB is a generous ceiling for a photo that will be downscaled to 512px anyway.
const AVATAR_FILE_RULES: FileValidationRules = {
    accept: "image/*",
    maxBytes: 10 * 1024 * 1024,
    maxMegapixels: DEFAULT_MAX_MEGAPIXELS,
};

interface EditableFieldProps {
    label: string;
    initialValue: string;
    validationField: "username" | "email";
    hint: string;
    type?: string;
    autoComplete?: string;
    submitting: boolean;
    externalError?: string | null;
    onEdit: () => void;
    onSubmit: (value: string) => void;
}

/**
 * An outlined text field that stays visually quiet until its value diverges from the
 * saved one, at which point inline save (✓) / discard (✕) affordances fade in. Enter
 * commits, Escape reverts. Validation errors are resolved from the `auth` namespace,
 * where the shared `validate()` helper's keys live.
 */
const EditableField: FC<EditableFieldProps> = ({
    label,
    initialValue,
    validationField,
    hint,
    type,
    autoComplete,
    submitting,
    externalError,
    onEdit,
    onSubmit,
}) => {
    const { t } = useTranslation(["settings", "auth"]);
    const [value, setValue] = useState(initialValue);
    const [fieldError, setFieldError] = useState<string | null>(null);

    const dirty = value.trim() !== initialValue.trim();
    const valid = !validate(validationField, value);

    const revert = () => {
        setValue(initialValue);
        setFieldError(null);
        onEdit();
    };

    const submit = () => {
        const err = validate(validationField, value);
        setFieldError(err);
        if (err || !dirty) return;
        onSubmit(value.trim());
    };

    const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
        if (event.key === "Enter") {
            event.preventDefault();
            submit();
        } else if (event.key === "Escape") {
            event.preventDefault();
            revert();
        }
    };

    const shownError = externalError ?? (fieldError ? t(fieldError, { ns: "auth" }) : null);

    return (
        <TextField
            value={value}
            onChange={(e) => {
                setValue(e.target.value);
                if (fieldError) setFieldError(null);
                onEdit();
            }}
            onBlur={(e) => {
                if (e.target.value.trim() !== initialValue.trim()) {
                    setFieldError(validate(validationField, e.target.value));
                }
            }}
            onKeyDown={handleKeyDown}
            error={!!shownError}
            helperText={shownError ?? hint}
            label={label}
            type={type}
            autoComplete={autoComplete}
            fullWidth
            slotProps={{
                input: {
                    endAdornment: (
                        <InputAdornment position="end">
                            {/* Discard is only meaningful once something has changed. */}
                            {dirty && (
                                <Tooltip title={t("discard")}>
                                    <IconButton
                                        size="small"
                                        onClick={revert}
                                        disabled={submitting}
                                        aria-label={t("discard")}
                                    >
                                        <CloseIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                            )}
                            {/* Save is always visible so the action is discoverable; it is
                                disabled until there is a valid, changed value to submit. */}
                            <Tooltip title={t("save")}>
                                <span>
                                    <IconButton
                                        size="small"
                                        color="primary"
                                        onClick={submit}
                                        disabled={!dirty || submitting || !valid}
                                        aria-label={t("save")}
                                    >
                                        <CheckIcon fontSize="small" />
                                    </IconButton>
                                </span>
                            </Tooltip>
                        </InputAdornment>
                    ),
                },
            }}
        />
    );
};

export const ProfileSection: FC = () => {
    const { t } = useTranslation("settings");
    const { user } = useAuthContext();
    const { isMobile } = useDeviceContext();
    const uploadAvatar = useUploadAvatarMutation();
    const removeAvatar = useDeleteAvatarMutation();
    const changeUsername = useChangeUsernameMutation();
    const requestEmailChange = useRequestEmailChangeMutation();

    // Holds the just-picked image as a data URL while it is being cropped; null when
    // no crop is in progress. The hidden file input is the native platform picker.
    const [pickedImage, setPickedImage] = useState<string | null>(null);
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [avatarError, setAvatarError] = useState<string | null>(null);
    const [usernameError, setUsernameError] = useState<string | null>(null);
    const [emailError, setEmailError] = useState<string | null>(null);
    const [emailSubmitted, setEmailSubmitted] = useState(false);
    // Remount key: after a successful email-change request the field clears back to
    // the current (still-unverified) email instead of keeping the pending input.
    const [emailFieldKey, setEmailFieldKey] = useState(0);

    // The avatar is a square whose side equals the height of the two-field column.
    // aspect-ratio + flexbox stretch does not reserve layout width reliably (the circle
    // overflows its track), so we measure the column and drive an explicit pixel size.
    const fieldsRef = useRef<HTMLDivElement>(null);
    const [fieldsHeight, setFieldsHeight] = useState(0);
    useLayoutEffect(() => {
        const element = fieldsRef.current;
        if (!element) return;
        const observer = new ResizeObserver((entries) => {
            setFieldsHeight(entries[0].contentRect.height);
        });
        observer.observe(element);
        return () => {
            observer.disconnect();
        };
    }, []);

    if (!user) return null;

    const avatarPx = isMobile ? AVATAR_PIXELS : fieldsHeight || AVATAR_PIXELS;

    const handleAvatarUpload = async (blob: Blob) => {
        try {
            setAvatarError(null);
            await uploadAvatar.mutateAsync(blob);
            setPickedImage(null);
        } catch {
            setAvatarError("avatar_upload_failed");
        }
    };

    // Opens the OS-native file picker. Reset value first so re-picking the same file
    // still fires `change` (the browser suppresses it when the value is unchanged).
    const openFilePicker = () => {
        if (fileInputRef.current) fileInputRef.current.value = "";
        fileInputRef.current?.click();
    };

    const handleFileSelected = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (!file) return;
        // Shared guards: `accept` is only a picker hint, so type/size/pixels are checked here.
        const validationError = await validateFile(file, AVATAR_FILE_RULES);
        if (validationError) {
            setAvatarError(validationError);
            return;
        }
        const reader = new FileReader();
        reader.onload = () => {
            setAvatarError(null);
            setPickedImage(typeof reader.result === "string" ? reader.result : null);
        };
        reader.onerror = () => {
            setAvatarError("avatar_upload_failed");
        };
        reader.readAsDataURL(file);
    };

    const handleAvatarRemove = () => {
        setAvatarError(null);
        removeAvatar.mutate(undefined, {
            onError: () => {
                setAvatarError("avatar_remove_failed");
            },
        });
    };

    const handleUsernameSubmit = (newUsername: string) => {
        setUsernameError(null);
        changeUsername.mutate(newUsername, {
            onError: (error) => {
                if (isAxiosError(error) && error.response?.status === 409) {
                    setUsernameError(t("username_conflict_error"));
                } else {
                    setUsernameError(t("username_update_failed"));
                }
            },
        });
    };

    const handleEmailSubmit = (newEmail: string) => {
        setEmailError(null);
        requestEmailChange.mutate(newEmail, {
            onSuccess: () => {
                setEmailSubmitted(true);
                setEmailFieldKey((key) => key + 1);
            },
            onError: (error) => {
                if (isAxiosError(error) && error.response?.status === 409) {
                    setEmailError(t("email_conflict_error"));
                } else {
                    setEmailError(t("email_change_failed"));
                }
            },
        });
    };

    const hasCustomAvatar = !!user.avatarUrl;

    return (
        <Box>
            <Typography variant="overline" color="text.secondary">
                {t("profile_section_title")}
            </Typography>
            <Divider sx={{ mb: 3 }} />

            {avatarError && (
                <Alert
                    severity="error"
                    sx={{ mb: 2 }}
                    onClose={() => {
                        setAvatarError(null);
                    }}
                >
                    {t(avatarError)}
                </Alert>
            )}
            {emailSubmitted && (
                <Alert
                    severity="info"
                    sx={{ mb: 2 }}
                    onClose={() => {
                        setEmailSubmitted(false);
                    }}
                >
                    {t("email_change_dispatched")}
                </Alert>
            )}

            <Stack direction={isMobile ? "column" : "row"} spacing={3} alignItems={isMobile ? "center" : "flex-start"}>
                {/* Square avatar sized in pixels to the measured field-column height, so it
                    reserves real layout width and never overlaps the fields. */}
                <Box sx={{ position: "relative", flexShrink: 0, width: avatarPx, height: avatarPx }}>
                    <Avatar
                        src={user.avatarUrl}
                        alt={user.name}
                        sx={{ width: "100%", height: "100%", fontSize: isMobile ? 28 : 40 }}
                    >
                        {user.name.slice(0, 2).toUpperCase()}
                    </Avatar>
                    <Tooltip title={t(hasCustomAvatar ? "avatar_change_button" : "avatar_upload_button")}>
                        <Box
                            role="button"
                            aria-label={t(hasCustomAvatar ? "avatar_change_button" : "avatar_upload_button")}
                            onClick={openFilePicker}
                            sx={{
                                position: "absolute",
                                inset: 0,
                                borderRadius: "50%",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                color: "common.white",
                                bgcolor: "rgba(0, 0, 0, 0.5)",
                                opacity: 0,
                                cursor: "pointer",
                                transition: "opacity 0.2s ease",
                                "&:hover, &:focus-visible": { opacity: 1 },
                            }}
                            tabIndex={0}
                            onKeyDown={(e) => {
                                if (e.key === "Enter" || e.key === " ") {
                                    e.preventDefault();
                                    openFilePicker();
                                }
                            }}
                        >
                            <PhotoCameraOutlinedIcon />
                        </Box>
                    </Tooltip>
                    {hasCustomAvatar && (
                        <Tooltip title={t("avatar_remove_button")}>
                            <IconButton
                                size="small"
                                onClick={handleAvatarRemove}
                                disabled={removeAvatar.isPending}
                                aria-label={t("avatar_remove_button")}
                                sx={{
                                    position: "absolute",
                                    bottom: -4,
                                    right: -4,
                                    bgcolor: "background.paper",
                                    border: "1px solid",
                                    borderColor: "divider",
                                    "&:hover": { bgcolor: "background.paper" },
                                }}
                            >
                                <DeleteOutlineIcon fontSize="small" />
                            </IconButton>
                        </Tooltip>
                    )}
                </Box>

                <Stack ref={fieldsRef} spacing={2} sx={{ flexGrow: 1, minWidth: 0, width: isMobile ? "100%" : "auto" }}>
                    <EditableField
                        label={t("username_label")}
                        initialValue={user.name}
                        validationField="username"
                        hint={t("username_hint")}
                        autoComplete="username"
                        submitting={changeUsername.isPending}
                        externalError={usernameError}
                        onEdit={() => {
                            setUsernameError(null);
                        }}
                        onSubmit={handleUsernameSubmit}
                    />
                    <EditableField
                        key={emailFieldKey}
                        label={t("email_label")}
                        initialValue={user.email}
                        validationField="email"
                        hint={t("email_change_hint")}
                        type="email"
                        autoComplete="email"
                        submitting={requestEmailChange.isPending}
                        externalError={emailError}
                        onEdit={() => {
                            setEmailSubmitted(false);
                            setEmailError(null);
                        }}
                        onSubmit={handleEmailSubmit}
                    />
                </Stack>
            </Stack>

            <Divider sx={{ my: 3 }} />
            <PasswordSection />

            {/* Native single-image picker; invoked before the crop dialog is shown. */}
            <input ref={fileInputRef} type="file" accept="image/*" hidden onChange={(e) => void handleFileSelected(e)} />
            <AvatarCropDialog
                imageSrc={pickedImage}
                onClose={() => {
                    setPickedImage(null);
                }}
                onConfirm={handleAvatarUpload}
                submitting={uploadAvatar.isPending}
            />
        </Box>
    );
};
