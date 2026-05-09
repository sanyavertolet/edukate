import { Alert, Collapse } from "@mui/material";
import { useState } from "react";
import { useTranslation } from "react-i18next";

const DISMISSED_KEY = "edukate:problem-sets-banner-dismissed";

export function ProblemSetWelcomeBanner() {
    const { t } = useTranslation("problem-sets");
    const [open, setOpen] = useState(() => localStorage.getItem(DISMISSED_KEY) !== "true");

    const handleClose = () => {
        setOpen(false);
        localStorage.setItem(DISMISSED_KEY, "true");
    };

    return (
        <Collapse in={open} unmountOnExit>
            <Alert severity="info" variant="outlined" onClose={handleClose} sx={{ mb: 1 }}>
                {t("welcome_banner")}
            </Alert>
        </Collapse>
    );
}
