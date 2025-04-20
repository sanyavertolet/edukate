import {Alert, Snackbar} from "@mui/material";
import {useEffect, useState} from "react";

interface ErrorSnackbarProps {
    errorText: string | undefined;
    autoHideDuration?: number;
    severity?: "error" | "warning" | "info" | "success";
    variant?: "filled" | "outlined" | "standard";
}

export function ErrorSnackbar(
    { errorText, autoHideDuration = 5000, severity = "error", variant = "standard" }: ErrorSnackbarProps
) {
    const [isOpen, setIsOpen] = useState(true);
    useEffect(() => { setIsOpen(errorText != undefined); }, [errorText]);
    const handleClose = () => { setIsOpen(false) };
    return (
        <Snackbar autoHideDuration={autoHideDuration} onClose={handleClose} open={isOpen && !!errorText}>
            <Alert onClose={handleClose} severity={severity} variant={variant} sx={{ width: '100%' }}>
                { errorText }
            </Alert>
        </Snackbar>
    );
}
