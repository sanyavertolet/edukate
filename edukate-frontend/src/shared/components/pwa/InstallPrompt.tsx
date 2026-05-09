import { useState } from "react";
import { Button, IconButton, Snackbar } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import { useTranslation } from "react-i18next";
import { usePwaContext } from "@/shared/context/PwaContext";

export function InstallPrompt() {
    const { canInstall, promptInstall } = usePwaContext();
    const { t } = useTranslation("common");
    const [dismissed, setDismissed] = useState(false);

    return (
        <Snackbar
            open={canInstall && !dismissed}
            anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
            message={t("install_prompt")}
            action={
                <>
                    <Button color="secondary" size="small" onClick={promptInstall}>
                        {t("install_button")}
                    </Button>
                    <IconButton
                        size="small"
                        color="inherit"
                        onClick={() => {
                            setDismissed(true);
                        }}
                    >
                        <CloseIcon fontSize="small" />
                    </IconButton>
                </>
            }
        />
    );
}
