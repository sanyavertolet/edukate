import { Snackbar } from "@mui/material";
import { useState } from "react";

interface PendingStatusSnackbarProps {
    open: boolean;
}

export function PendingStatusSnackbar({open}: PendingStatusSnackbarProps) {
    const [isOpen, setIsOpen] = useState(true);

    const handleClose = () => { setIsOpen(false) };

    return (
        <Snackbar
            open={open && isOpen}
            autoHideDuration={5000}
            onClose={handleClose}
            message={"Your account is pending approval. Some features are temporarily restricted."}
        />
    )
}
