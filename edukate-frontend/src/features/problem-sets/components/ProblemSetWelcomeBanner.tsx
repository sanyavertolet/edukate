import { Alert, Collapse } from "@mui/material";
import { useState } from "react";

const DISMISSED_KEY = "edukate:problem-sets-banner-dismissed";

export function ProblemSetWelcomeBanner() {
    const [open, setOpen] = useState(() => localStorage.getItem(DISMISSED_KEY) !== "true");

    const handleClose = () => {
        setOpen(false);
        localStorage.setItem(DISMISSED_KEY, "true");
    };

    return (
        <Collapse in={open} unmountOnExit>
            <Alert severity="info" variant="outlined" onClose={handleClose} sx={{ mb: 1 }}>
                Problem sets let you organize, share, and collaborate on curated problem collections. Browse public sets or
                create your own!
            </Alert>
        </Collapse>
    );
}
