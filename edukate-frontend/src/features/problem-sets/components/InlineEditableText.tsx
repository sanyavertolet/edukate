import { FC, KeyboardEvent, ReactNode, useEffect, useRef, useState } from "react";
import { Box, IconButton, Stack, TextField, Tooltip, Typography, TypographyProps } from "@mui/material";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";

interface InlineEditableTextProps {
    value: string;
    onSave: (next: string) => void;
    disabled?: boolean;
    multiline?: boolean;
    minLength?: number;
    maxLength?: number;
    placeholder?: string;
    typographyProps?: TypographyProps;
    editTooltip?: string;
    emptyText?: string;
    pending?: boolean;
    endAdornment?: ReactNode;
}

export const InlineEditableText: FC<InlineEditableTextProps> = ({
    value,
    onSave,
    disabled = false,
    multiline = false,
    minLength = 0,
    maxLength,
    placeholder,
    typographyProps,
    editTooltip,
    emptyText,
    pending = false,
    endAdornment,
}) => {
    const [isEditing, setIsEditing] = useState(false);
    const [draft, setDraft] = useState(value);
    const inputRef = useRef<HTMLInputElement | HTMLTextAreaElement>(null);

    useEffect(() => {
        if (!isEditing) setDraft(value);
    }, [value, isEditing]);

    const trimmed = draft.trim();
    const tooShort = trimmed.length < minLength;
    const tooLong = maxLength != null && draft.length > maxLength;
    const hasError = tooShort || tooLong;

    const commit = () => {
        if (hasError) return;
        if (trimmed !== value.trim()) onSave(trimmed);
        setIsEditing(false);
    };

    const revert = () => {
        setDraft(value);
        setIsEditing(false);
    };

    const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
        if (event.key === "Escape") {
            event.preventDefault();
            revert();
        } else if (event.key === "Enter" && !multiline) {
            event.preventDefault();
            commit();
        }
    };

    if (isEditing) {
        return (
            <TextField
                inputRef={inputRef}
                value={draft}
                onChange={(e) => {
                    setDraft(e.target.value);
                }}
                onBlur={commit}
                onKeyDown={handleKeyDown}
                onFocus={(e) => {
                    e.target.select();
                }}
                autoFocus
                fullWidth
                size="small"
                variant="standard"
                multiline={multiline}
                placeholder={placeholder}
                error={hasError}
                helperText={
                    tooShort
                        ? `At least ${String(minLength)} character${minLength === 1 ? "" : "s"} required`
                        : tooLong
                          ? `Maximum ${String(maxLength)} characters`
                          : undefined
                }
                disabled={pending}
                slotProps={{ htmlInput: { maxLength: maxLength != null ? maxLength + 1 : undefined } }}
            />
        );
    }

    const displayValue = value || emptyText || placeholder || "";
    return (
        <Stack direction="row" alignItems="center" spacing={0.5} sx={{ width: "100%" }}>
            <Box
                onClick={() => {
                    if (!disabled) setIsEditing(true);
                }}
                sx={{
                    flexGrow: 1,
                    cursor: disabled ? "default" : "text",
                    borderRadius: 1,
                    px: 0.5,
                    "&:hover": disabled ? {} : { bgcolor: "action.hover" },
                }}
            >
                <Typography
                    {...typographyProps}
                    color={value ? typographyProps?.color : "text.disabled"}
                    sx={{ whiteSpace: multiline ? "pre-wrap" : "normal" }}
                >
                    {displayValue}
                </Typography>
            </Box>
            {endAdornment}
            {!disabled && (
                <Tooltip title={editTooltip ?? "Edit"}>
                    <IconButton
                        size="small"
                        onClick={() => {
                            setIsEditing(true);
                        }}
                    >
                        <EditOutlinedIcon fontSize="inherit" />
                    </IconButton>
                </Tooltip>
            )}
        </Stack>
    );
};
