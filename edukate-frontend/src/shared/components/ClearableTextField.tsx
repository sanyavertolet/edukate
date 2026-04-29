import { FC } from "react";
import { TextField, IconButton, InputAdornment } from "@mui/material";
import ClearIcon from "@mui/icons-material/Clear";
import type { SxProps, Theme } from "@mui/material";

type ClearableTextFieldProps = {
    label: string;
    value: string;
    onChange: (value: string) => void;
    onClear: () => void;
    sx?: SxProps<Theme>;
};

export const ClearableTextField: FC<ClearableTextFieldProps> = ({ label, value, onChange, onClear, sx }) => {
    return (
        <TextField
            label={label}
            size="small"
            value={value}
            onChange={(e) => {
                onChange(e.target.value);
            }}
            sx={sx}
            slotProps={{
                input: {
                    endAdornment: (
                        <InputAdornment position="end">
                            <IconButton size="small" onClick={onClear} sx={{ visibility: value ? "visible" : "hidden" }}>
                                <ClearIcon fontSize="small" />
                            </IconButton>
                        </InputAdornment>
                    ),
                },
            }}
        />
    );
};
